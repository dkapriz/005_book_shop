package com.example.bookshopapp.exception;

import com.example.bookshopapp.api.response.ResultResponse;
import lombok.Getter;

@Getter
public class CheckCodeException extends Exception {
    private ResultResponse resultResponse;

    public CheckCodeException(String message) {
        super(message);
    }

    public CheckCodeException(ResultResponse resultResponse) {
        super(resultResponse.getErrorMessage());
        this.resultResponse = resultResponse;
    }
}