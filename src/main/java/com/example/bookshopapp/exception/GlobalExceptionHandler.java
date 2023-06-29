package com.example.bookshopapp.exception;

import com.example.bookshopapp.api.response.ApiResponse;
import com.example.bookshopapp.api.response.BookListResponse;
import com.example.bookshopapp.api.response.ResultResponse;
import javassist.tools.reflect.CannotCreateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.csrf.MissingCsrfTokenException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.util.WebUtils;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<BookListResponse>> handleMissingServletRequestParameterException(Exception ex) {
        log.warn(ex.getLocalizedMessage());
        return new ResponseEntity<>(new ApiResponse<>(HttpStatus.BAD_REQUEST, "Missing required parameters",
                ex), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BookListWrongParameterException.class)
    public ResponseEntity<ApiResponse<BookListResponse>> handleBookListWrongParameterException(Exception ex) {
        log.warn(ex.getLocalizedMessage());
        return new ResponseEntity<>(new ApiResponse<>(HttpStatus.BAD_REQUEST, "Bad parameter value...",
                ex), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ViewEmptyParameterException.class)
    public String handleViewEmptyParameterException(ViewEmptyParameterException ex) {
        log.warn(ex.getLocalizedMessage());
        return "redirect:/";
    }

    @ExceptionHandler(CannotCreateException.class)
    public String handleCannotCreateException(CannotCreateException ex) {
        log.warn(ex.getLocalizedMessage());
        return "redirect:/";
    }

    @ExceptionHandler(ViewNotFoundParameterException.class)
    public String handleViewNotFoundParameterException(CannotCreateException ex) {
        log.warn(ex.getLocalizedMessage());
        return "redirect:/error";
    }

    @ExceptionHandler({
            UsernameNotFoundException.class,
            AuthenticationException.class,
            MissingCsrfTokenException.class,
            WrongParameterException.class,
            SendCodeException.class,
            CheckCodeException.class,
            SendSMSException.class,
            PaymentException.class
    })
    public final ResponseEntity<ApiResponse<ResultResponse>> handleException(Exception ex, WebRequest request) {
        HttpHeaders httpHeaders = new HttpHeaders();

        if (ex instanceof UsernameNotFoundException) {
            HttpStatus httpStatus = HttpStatus.NOT_FOUND;
            UsernameNotFoundException usernameNotFoundException = (UsernameNotFoundException) ex;
            log.info("UsernameNotFoundException " + ex.getMessage());
            return handleResultErrorException(usernameNotFoundException, httpHeaders, httpStatus, request);
        }

        if (ex instanceof AuthenticationException) {
            HttpStatus httpStatus = HttpStatus.UNAUTHORIZED;
            AuthenticationException authenticationException = (AuthenticationException) ex;
            log.info("AuthenticationException " + ex.getMessage());
            return handleResultErrorException(authenticationException, httpHeaders, httpStatus, request);
        }

        if (ex instanceof MissingCsrfTokenException) {
            HttpStatus httpStatus = HttpStatus.UNAUTHORIZED;
            MissingCsrfTokenException missingCsrfTokenException = (MissingCsrfTokenException) ex;
            log.info("MissingCsrfTokenException " + ex.getMessage());
            return handleResultErrorException(missingCsrfTokenException, httpHeaders, httpStatus, request);
        }

        if (ex instanceof WrongParameterException) {
            HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
            WrongParameterException wrongParameterException = (WrongParameterException) ex;
            log.info("WrongParameterException " + ex.getMessage());
            return handleResultErrorException(wrongParameterException, httpHeaders, httpStatus, request);
        }

        if (ex instanceof SendCodeException) {
            HttpStatus httpStatus = HttpStatus.OK;
            SendCodeException sendCodeException = (SendCodeException) ex;
            log.info("SendCodeException " + ex.getMessage());
            return handleResultErrorException(sendCodeException, httpHeaders, httpStatus, request);
        }

        if (ex instanceof CheckCodeException) {
            HttpStatus httpStatus = HttpStatus.OK;
            CheckCodeException checkCodeException = (CheckCodeException) ex;
            log.info("CheckCodeException " + ex.getMessage());
            return handleResultErrorException(checkCodeException, checkCodeException.getResultResponse(),
                    httpHeaders, httpStatus, request);
        }

        if (ex instanceof SendSMSException) {
            HttpStatus httpStatus = HttpStatus.OK;
            SendSMSException sendSMSException = (SendSMSException) ex;
            log.info("SendSMSException " + ex.getMessage());
            return handleResultErrorException(sendSMSException, httpHeaders, httpStatus, request);
        }

        if (ex instanceof PaymentException) {
            HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
            PaymentException paymentException = (PaymentException) ex;
            log.info("PaymentException " + ex.getMessage());
            return handleResultErrorException(paymentException, httpHeaders, httpStatus, request);
        }

        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        log.error(ex.getMessage());
        return handleExceptionInternal(ex, null, httpHeaders, httpStatus, request);
    }

    private ResponseEntity<ApiResponse<ResultResponse>> handleResultErrorException
            (Exception ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        ApiResponse<ResultResponse> response = new ApiResponse<>();
        response.setDebugMessage("successful request");
        response.setStatus(status);
        response.setData(new ResultResponse(ex.getMessage()));
        return handleExceptionInternal(ex, response, headers, status, request);
    }

    private ResponseEntity<ApiResponse<ResultResponse>> handleResultErrorException
            (Exception ex, ResultResponse resultResponse, HttpHeaders headers, HttpStatus status, WebRequest request) {
        ApiResponse<ResultResponse> response = new ApiResponse<>();
        response.setDebugMessage("successful request");
        response.setStatus(status);
        response.setData(resultResponse);
        return handleExceptionInternal(ex, response, headers, status, request);
    }

    protected ResponseEntity<ApiResponse<ResultResponse>> handleExceptionInternal
            (Exception ex, ApiResponse<ResultResponse> body, HttpHeaders headers,
             HttpStatus status, WebRequest request) {
        if (HttpStatus.INTERNAL_SERVER_ERROR.equals(status)) {
            request.setAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE, ex.getMessage(), RequestAttributes.SCOPE_REQUEST);
        }
        ex.printStackTrace();
        return new ResponseEntity<>(body, headers, status);
    }
}
