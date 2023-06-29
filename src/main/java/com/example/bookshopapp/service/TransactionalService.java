package com.example.bookshopapp.service;

import com.example.bookshopapp.api.dto.TransactionalDto;
import com.example.bookshopapp.api.request.PayRequest;
import com.example.bookshopapp.api.response.TransactionalListResponse;
import com.example.bookshopapp.aspect.ValidateParamsView;
import com.example.bookshopapp.config.BookShopConfig;
import com.example.bookshopapp.config.LanguageMessage;
import com.example.bookshopapp.exception.PaymentException;
import com.example.bookshopapp.exception.TransactionalException;
import com.example.bookshopapp.exception.ViewEmptyParameterException;
import com.example.bookshopapp.model.BalanceTransaction;
import com.example.bookshopapp.model.Book;
import com.example.bookshopapp.model.User;
import com.example.bookshopapp.model.enums.BookStatus;
import com.example.bookshopapp.repositories.BalanceTransactionRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.example.bookshopapp.config.BookShopConfig.SORT_PARAM_TRANSACTION;
import static com.example.bookshopapp.config.LanguageMessage.EX_MSG_USER_NOT_FOUND;
import static com.example.bookshopapp.config.LanguageMessage.EX_MSG_WRONG_PARAM;
import static org.springframework.transaction.annotation.Isolation.SERIALIZABLE;
import static org.springframework.web.context.WebApplicationContext.SCOPE_SESSION;

@Service
@Scope(value = SCOPE_SESSION)
public class TransactionalService {

    public static final String SORT_ASC = "asc";
    public static final String SORT_DESC = "desc";
    private final PaymentService paymentService;
    private final AuthService authService;
    private final BookService bookService;
    private final BookStatusService bookStatusService;
    private final BalanceTransactionRepository balanceTransactionRepository;
    private final BookShopConfig config;

    @Autowired
    public TransactionalService(PaymentService paymentService, AuthService authService, BookService bookService,
                                BookStatusService bookStatusService, BookShopConfig config,
                                BalanceTransactionRepository balanceTransactionRepository) {
        this.paymentService = paymentService;
        this.authService = authService;
        this.bookService = bookService;
        this.bookStatusService = bookStatusService;
        this.balanceTransactionRepository = balanceTransactionRepository;
        this.config = config;
    }

    /**
     * Метод покупки книг
     * Если баланс пользователя меньше стоимости книг, то происходит перенаправление на платежную систему на сумму не
     * хватающую для оплаты. В данном случае метод возвращает URI адрес на оплату.
     * Если баланс пользователя достаточен для покупки, то книги из корзины переводятся в статус PAID и уменьшается
     * баланс пользователя на сумму покупки. При успешном выполнении операции возвращается пустое значение.
     *
     * @return URI ссылка на оплату и пустое значение
     * @throws PaymentException ошибка платежной системы
     */
    @Transactional(isolation = SERIALIZABLE, propagation = Propagation.REQUIRES_NEW)
    public String handleCartPaid() throws PaymentException {
        User user = authService.getCurrentUser();
        if (user == null) {
            throw new UsernameNotFoundException(EX_MSG_USER_NOT_FOUND);
        }
        CartBooks cartBooks = new CartBooks(user);
        if (cartBooks.getAmount() > user.getBalance()) {
            return paymentService.topUpAccountBalance(
                    new PayRequest(user.getHash(), String.valueOf(cartBooks.getAmount() - user.getBalance()),
                            System.currentTimeMillis()), config.getPaymentRedirectUriCart());
        }
        changeStatusBookAndUserBalance(user, cartBooks);
        addTransactional(user, cartBooks);
        return "";
    }

    @ValidateParamsView
    public TransactionalListResponse getTransactionalList(String sort, Integer offset, Integer limit)
            throws ViewEmptyParameterException {
        if (!sort.equals(SORT_ASC) && !sort.equals(SORT_DESC)) {
            throw new ViewEmptyParameterException(EX_MSG_WRONG_PARAM);
        }
        Pageable pageable = PageRequest.of(offset, limit, sort.equals(SORT_ASC) ?
                Sort.by(SORT_PARAM_TRANSACTION).ascending() : Sort.by(SORT_PARAM_TRANSACTION).descending());
        User user = authService.getCurrentUser();
        if (user == null) {
            throw new UsernameNotFoundException(EX_MSG_USER_NOT_FOUND);
        }
        Page<BalanceTransaction> transactions = balanceTransactionRepository.findAllByUser(user, pageable);
        return getTransactionList(transactions);
    }

    private TransactionalListResponse getTransactionList(Page<BalanceTransaction> balanceTransactions) {
        TransactionalListResponse transactionalListResponse = new TransactionalListResponse();
        transactionalListResponse.setCount((int) balanceTransactions.getTotalElements());
        List<TransactionalDto> transactionalDtoList = new ArrayList<>();
        for (BalanceTransaction transaction : balanceTransactions) {
            TransactionalDto transactionalDto = new TransactionalDto();
            transactionalDto.setValue(transaction.getValue());
            transactionalDto.setDescription(transaction.getDescription());
            transactionalDto.setTime(String.valueOf(Timestamp.valueOf(transaction.getTime())));
            transactionalDto.setLocalDateTime(transaction.getTime());
            transactionalDtoList.add(transactionalDto);
        }
        transactionalListResponse.setTransactions(transactionalDtoList);
        return transactionalListResponse;
    }

    private void changeStatusBookAndUserBalance(User user, CartBooks cartBooks) {
        for (Book book : cartBooks.getCartBookList()) {
            bookStatusService.changeOrCreateBook2UserLink(user, book.getId(), BookStatus.PAID);
            user.setBalance(user.getBalance() - BookService.getDiscountPrice(book.getPrice(), book.getDiscount()));
            if (user.getBalance() < 0) {
                throw new TransactionalException("The user's balance is less than the cost of the book");
            }
        }
    }

    private void addTransactional(User user, CartBooks cartBooks) {
        for (Book book : cartBooks.getCartBookList()) {
            BalanceTransaction balanceTransaction = new BalanceTransaction();
            balanceTransaction.setValue(BookService.getDiscountPrice(book.getPrice(), book.getDiscount()) * -1);
            balanceTransaction.setDescription(LanguageMessage
                    .getMsgPaidBookDescription(book.getTitle(), book.getSlug()));
            balanceTransaction.setUser(user);
            balanceTransaction.setBook(book);
            balanceTransaction.setTime(LocalDateTime.now());
            balanceTransactionRepository.save(balanceTransaction);
        }
    }

    @Getter
    private class CartBooks {
        private final List<Book> cartBookList;
        private int amount;

        protected CartBooks(User user) {
            cartBookList = bookService.getListBooksByStatus(BookStatus.CART, user);
            amount = 0;
            for (Book book : cartBookList) {
                amount += BookService.getDiscountPrice(book.getPrice(), book.getDiscount());
            }
        }
    }
}
