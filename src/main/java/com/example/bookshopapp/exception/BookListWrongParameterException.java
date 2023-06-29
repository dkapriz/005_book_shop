package com.example.bookshopapp.exception;

public class BookListWrongParameterException extends RuntimeException {
    public BookListWrongParameterException(String massage) {
        super(massage);
    }
}
