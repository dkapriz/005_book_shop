package com.example.bookshopapp.exception;

public class TransactionalException extends RuntimeException {
    public TransactionalException(String massage) {
        super(massage);
    }
}