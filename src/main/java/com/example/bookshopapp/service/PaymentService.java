package com.example.bookshopapp.service;

import com.example.bookshopapp.api.request.PayRequest;
import com.example.bookshopapp.exception.PaymentException;

public interface PaymentService {
    String topUpAccountBalance(PayRequest payRequest, String redirectURI) throws PaymentException;
}