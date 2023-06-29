package com.example.bookshopapp.service;

import com.example.bookshopapp.api.dto.payment.Amount;
import com.example.bookshopapp.api.dto.payment.Confirmation;
import com.example.bookshopapp.api.request.PayRequest;
import com.example.bookshopapp.api.request.payment.PaymentRequest;
import com.example.bookshopapp.api.response.payment.PaymentResponse;
import com.example.bookshopapp.aspect.ValidateParamsRest;
import com.example.bookshopapp.config.BookShopConfig;
import com.example.bookshopapp.config.LanguageMessage;
import com.example.bookshopapp.exception.PaymentException;
import com.example.bookshopapp.exception.TransactionalException;
import com.example.bookshopapp.model.BalanceTransaction;
import com.example.bookshopapp.model.User;
import com.example.bookshopapp.model.YooCashOperation;
import com.example.bookshopapp.repositories.BalanceTransactionRepository;
import com.example.bookshopapp.repositories.UserRepository;
import com.example.bookshopapp.repositories.YooCashOperationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;

import static com.example.bookshopapp.config.BookShopConfig.*;
import static com.example.bookshopapp.config.LanguageMessage.EX_MSG_PAYMENT_SERVICE_ERROR;
import static com.example.bookshopapp.config.LanguageMessage.EX_MSG_USER_NOT_FOUND;
import static org.springframework.transaction.annotation.Isolation.SERIALIZABLE;
import static org.springframework.web.context.WebApplicationContext.SCOPE_SESSION;

@Service
@Slf4j
@Scope(value = SCOPE_SESSION)
public class PaymentServiceYooCashImpl implements PaymentService {
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_SUCCEEDED = "succeeded";
    public static final String STATUS_CANCELED = "canceled";
    public static final String STATUS_WAITING = "waiting_for_capture";

    private final AuthService authService;
    private final UserRepository userRepository;
    private final BalanceTransactionRepository balanceTransactionRepository;
    private final WebClient webClient;
    private final YooCashOperationRepository yooCashOperationRepository;

    private final BookShopConfig config;
    private static final Random random = new Random();
    private final CachingPayment cachingPayment;
    private final ConcurrentLinkedQueue<String> confirmationPaidQueue;

    @Autowired
    public PaymentServiceYooCashImpl(AuthService authService, UserRepository userRepository,
                                     BalanceTransactionRepository balanceTransactionRepository, WebClient webClient,
                                     YooCashOperationRepository yooCashOperationRepository, BookShopConfig config) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.balanceTransactionRepository = balanceTransactionRepository;
        this.webClient = webClient;
        this.yooCashOperationRepository = yooCashOperationRepository;
        this.config = config;
        cachingPayment = new CachingPayment();
        confirmationPaidQueue = new ConcurrentLinkedQueue<>();
    }

    /**
     * Метод создает запрос в платежную систему Ю Касса и получает ссылку на оплату
     * Метод обеспечивает идемпотентность в течение времени, установленного в
     * TIME_CACHING_PAYMENT_REQUEST_SEC (в секундах)
     * Запрос на оплату сохраняется в базе данных со статусом Pending
     * Метод выполняется в асинхронном режиме. Входящий запрос с результатом ответа сохраняется в HashMap.
     * Во время выполнения метода проверяется аналогичный запрос в HashMap в течении установленного времени,
     * если запрос найден, возвращается кешируемый результат.
     *
     * @param payRequest параметры запроса
     * @return URI перенаправления на платежную систему
     */
    public String topUpAccountBalance(PayRequest payRequest, String redirectURI) throws PaymentException {
        payRequest.setSum(payRequest.getSum() + ".00");
        if (cachingPayment.isExist(payRequest)) {
            String uri;
            try {
                uri = cachingPayment.getValue(payRequest);
            } catch (InterruptedException | ExecutionException e) {
                Thread.currentThread().interrupt();
                throw new PaymentException(EX_MSG_PAYMENT_SERVICE_ERROR);
            }
            log.info("RETURN CACHING PAYMENT URI:" + uri);
            return uri;
        }
        CompletableFuture<String> completableFuture = new CompletableFuture<>();
        cachingPayment.add(payRequest, completableFuture);
        completableFuture = topUpAccountBalanceAsync(payRequest.getHash(),
                payRequest.getSum(), payRequest.getTime(), completableFuture, redirectURI);
        CompletableFuture.allOf(completableFuture).join();
        try {
            return completableFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new PaymentException(EX_MSG_PAYMENT_SERVICE_ERROR);
        }
    }

    /**
     * Метод получает подтверждение платежа путем периодических запросов, частота которых определяется
     * параметром DELAY_PAYMENT_CONFIRMATION. Идентификаторы платежей, по которым необходимо получить подтверждение,
     * хранятся в очереди. Метод последовательно извлекает платежи из очереди и отправляет запрос в платежную систему
     * о состоянии платежа. После получения ответа из платежной системы, изменяется статус платежа в таблице Ю Касса,
     * а так же сохраняется результат транзакции и обновляется баланс пользователя.
     *
     * @throws TransactionalException происходит откат операции
     */
    @Scheduled(fixedDelay = DELAY_PAYMENT_CONFIRMATION)
    @Transactional(isolation = SERIALIZABLE, propagation = Propagation.REQUIRES_NEW)
    public void getPaymentConfirmation() throws TransactionalException {
        while (!confirmationPaidQueue.isEmpty()) {
            String currentId = confirmationPaidQueue.peek();
            Optional<YooCashOperation> yooCashOperation = yooCashOperationRepository.findByOperationId(currentId);
            if (!yooCashOperation.isPresent()) {
                log.error("There is no record of payment in the database with id: " + currentId +
                        " The missing record has been removed from the payment processing queue");
                confirmationPaidQueue.remove(currentId);
                continue;
            }
            PaymentResponse response = sendPaymentConfirmationRequestForYooCash(currentId);
            if (response == null) {
                log.error("Invalid response from the payment system server. Payment id" + currentId +
                        " The missing record has been removed from the payment processing queue");
                confirmationPaidQueue.remove(currentId);
                continue;
            }
            if (response.getStatus().equals(STATUS_PENDING)) {
                continue;
            }
            if (response.getStatus().equals(STATUS_WAITING)) {
                log.warn("Payment system parameters are incorrectly configured. Payments are transferred to the " +
                        "status of waiting_for_capture");
                confirmationPaidQueue.remove(currentId);
                continue;
            }
            processingPaymentTransaction(response);
        }
    }

    @Async
    @ValidateParamsRest
    public CompletableFuture<String> topUpAccountBalanceAsync(
            String hash, String sum, Long timestamp, CompletableFuture<String> completableFuture, String redirectURI)
            throws PaymentException {
        User user = authService.getCurrentUser();
        if (user == null || !user.getHash().equals(hash)) {
            throw new UsernameNotFoundException(EX_MSG_USER_NOT_FOUND);
        }
        LocalDateTime time = getLocalDateTimeFromTimestamp(timestamp);
        String idempotenceKey = generateIdempotenceKey();
        PaymentResponse response = sendPaymentRequestForYooCash(sum,
                user.getUserContacts().get(0).getContact(), idempotenceKey, redirectURI);
        if (response == null || !response.getStatus().equals(STATUS_PENDING)) {
            throw new PaymentException(EX_MSG_PAYMENT_SERVICE_ERROR);
        }
        saveYooCashOperationRequest(response, idempotenceKey, time, user);
        confirmationPaidQueue.add(response.getId());
        log.info("CREATE PAYMENT with id: " + response.getId() + " idempotency key: " + idempotenceKey +
                " confirmation payment URI: " + response.getConfirmation().getConfirmationUrl());
        completableFuture.complete(response.getConfirmation().getConfirmationUrl());
        return completableFuture;
    }

    private PaymentResponse sendPaymentRequestForYooCash(String sum, String userContact, String idempotenceKey,
                                                         String redirectURI)
            throws PaymentException {
        ResponseEntity<PaymentResponse> response = webClient
                .post()
                .uri(config.getPaymentUri())
                .headers(httpHeaders -> httpHeaders
                        .setBasicAuth(config.getPaymentId(), config.getPaymentSecret()))
                .header(IDEMPOTENCE_KEY_NAME, idempotenceKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(getPaymentForYooCash(sum, userContact, redirectURI)), PaymentRequest.class)
                .retrieve()
                .toEntity(PaymentResponse.class)
                .block();
        if (response == null || !response.getStatusCode().is2xxSuccessful()) {
            throw new PaymentException(EX_MSG_PAYMENT_SERVICE_ERROR);
        }
        log.info("A payment request has been sent to the payment system for the contact: " + userContact);
        return response.getBody();
    }

    private PaymentResponse sendPaymentConfirmationRequestForYooCash(String id) {
        ResponseEntity<PaymentResponse> response = webClient
                .get()
                .uri(config.getPaymentUri() + "/" + id)
                .headers(httpHeaders -> httpHeaders
                        .setBasicAuth(config.getPaymentId(), config.getPaymentSecret()))
                .retrieve()
                .toEntity(PaymentResponse.class)
                .block();
        if (response == null || !response.getStatusCode().is2xxSuccessful()) {
            return null;
        }
        log.info("Payment confirmation received for id: " + id);
        return response.getBody();
    }

    private PaymentRequest getPaymentForYooCash(String sum, String userContact, String redirectURI) {
        PaymentRequest request = new PaymentRequest();
        request.setAmount(new Amount(sum, config.getPaymentCurrency()));
        request.setCapture(true);
        request.setConfirmation(new Confirmation("redirect", redirectURI, null));
        request.setDescription(LanguageMessage.getMsgReceiptDescription(userContact));
        return request;
    }

    private void saveYooCashOperationRequest(PaymentResponse response, String idempotencyKey,
                                             LocalDateTime time, User user) {
        YooCashOperation yooCashOperation = yooCashOperationRepository
                .findByOperationId(response.getId()).orElse(new YooCashOperation());
        yooCashOperation.setOperationId(response.getId());
        yooCashOperation.setSum(response.getAmount().getValue());
        yooCashOperation.setStatus(response.getStatus());
        yooCashOperation.setIdempotencyKey(idempotencyKey);
        yooCashOperation.setTime(time);
        yooCashOperation.setUser(user);
        yooCashOperationRepository.save(yooCashOperation);
    }

    private void processingPaymentTransaction(PaymentResponse response) throws TransactionalException {
        Optional<YooCashOperation> yooCashOperationOpt = yooCashOperationRepository
                .findByOperationId(response.getId());
        if (yooCashOperationOpt.isPresent()) {
            YooCashOperation yooCashOperation = yooCashOperationOpt.get();
            yooCashOperation.setStatus(response.getStatus());
            yooCashOperation.setPaymentMethod(response.getPaymentMethod().getType());
            confirmationPaidQueue.remove(response.getId());
            if (response.getStatus().equals(STATUS_CANCELED)) {
                return;
            }
            if (response.getStatus().equals(STATUS_SUCCEEDED)) {
                createTransactional(yooCashOperation.getUser(), yooCashOperation.getSum(),
                        response.getPaymentMethod().getType());
            }
        }
    }

    private void createTransactional(User user, String sum, String payType) throws TransactionalException {
        Optional<User> userOptional = userRepository.findById(user.getId());
        if (!userOptional.isPresent()) {
            throw new TransactionalException(EX_MSG_USER_NOT_FOUND);
        }
        User curUser = userOptional.get();
        int curSum = (int) Double.parseDouble(sum);
        if (curSum <= 0) {
            throw new TransactionalException(EX_MSG_USER_NOT_FOUND);
        }
        curUser.setBalance(curUser.getBalance() + curSum);
        userRepository.save(curUser);
        BalanceTransaction balanceTransaction = new BalanceTransaction();
        balanceTransaction.setUser(curUser);
        balanceTransaction.setTime(LocalDateTime.now());
        balanceTransaction.setValue(curSum);
        balanceTransaction.setDescription("Пополнение баланса пользователя: " + payType);
        balanceTransactionRepository.save(balanceTransaction);
    }

    private LocalDateTime getLocalDateTimeFromTimestamp(long time) {
        return Instant.ofEpochSecond(time / 1000).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    protected String generateIdempotenceKey() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < LENGTH_IDEMPOTENCE_KEY; i++) {
            sb.append(SYMBOLS[random.nextInt(SYMBOLS.length)]);
        }
        return sb.toString();
    }

    private static class CachingPayment {
        private final ConcurrentHashMap<PayRequest, CompletableFuture<String>> paymentRequests;

        public CachingPayment() {
            paymentRequests = new ConcurrentHashMap<>();
        }

        public void add(PayRequest key, CompletableFuture<String> value) {
            paymentRequests.put(key, value);
        }

        public boolean isExist(PayRequest key) {
            clear();
            return paymentRequests.containsKey(key);
        }

        public String getValue(PayRequest key) throws ExecutionException, InterruptedException {
            CompletableFuture<String> value = paymentRequests.get(key);
            return value.get();
        }

        public void clear() {
            for (PayRequest request : paymentRequests.keySet()) {
                if ((request.getTime() + TIME_CACHING_PAYMENT_REQUEST_SEC * 1000) < System.currentTimeMillis()) {
                    paymentRequests.remove(request);
                }
            }
        }
    }

}