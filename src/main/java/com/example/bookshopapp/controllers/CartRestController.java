package com.example.bookshopapp.controllers;

import com.example.bookshopapp.api.request.PayRequest;
import com.example.bookshopapp.api.response.ApiResponse;
import com.example.bookshopapp.api.response.RedirectResponse;
import com.example.bookshopapp.api.response.ResultResponse;
import com.example.bookshopapp.api.response.TransactionalListResponse;
import com.example.bookshopapp.config.BookShopConfig;
import com.example.bookshopapp.exception.PaymentException;
import com.example.bookshopapp.exception.ViewEmptyParameterException;
import com.example.bookshopapp.service.PaymentService;
import com.example.bookshopapp.service.TransactionalService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.example.bookshopapp.service.TransactionalService.SORT_ASC;
import static org.springframework.web.context.WebApplicationContext.SCOPE_SESSION;

@RestController
@RequestMapping("/api")
@Scope(value = SCOPE_SESSION)
@Api("cart REST controller")
public class CartRestController {

    private final PaymentService paymentService;
    private final TransactionalService transactionalService;
    private final BookShopConfig config;

    @Autowired
    public CartRestController(PaymentService paymentService, TransactionalService transactionalService,
                              BookShopConfig config) {
        this.paymentService = paymentService;
        this.transactionalService = transactionalService;
        this.config = config;
    }

    @PostMapping("/payment")
    @ApiOperation("redirection to the payment system")
    public ResponseEntity<ApiResponse<ResultResponse>> handlePaymentRedirection(
            @RequestBody PayRequest payRequest) throws PaymentException {
        String confirmationUri = paymentService.topUpAccountBalance(payRequest, config.getPaymentRedirectUriBalance());
        return getResultResponseStatus200(new RedirectResponse(true, true, confirmationUri));
    }

    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<TransactionalListResponse>> handleTransactionListResponse(
            @RequestParam(defaultValue = SORT_ASC) String sort,
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "5") Integer limit) throws ViewEmptyParameterException {
        return getResultResponseStatus200(transactionalService.getTransactionalList(sort, offset, limit));
    }

    private <T>ResponseEntity<ApiResponse<T>> getResultResponseStatus200(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setDebugMessage("successful request");
        response.setStatus(HttpStatus.OK);
        response.setData(data);
        return ResponseEntity.ok(response);
    }
}
