package com.example.bookshopapp.exception;

public class WrongParameterException extends RuntimeException {
    public WrongParameterException(String massage) {
        super(massage);
    }
}