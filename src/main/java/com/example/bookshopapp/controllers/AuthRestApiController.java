package com.example.bookshopapp.controllers;

import com.example.bookshopapp.api.request.ContactConfirmationPayload;
import com.example.bookshopapp.api.response.ApiResponse;
import com.example.bookshopapp.api.response.ResultResponse;
import com.example.bookshopapp.config.BookShopConfig;
import com.example.bookshopapp.exception.CheckCodeException;
import com.example.bookshopapp.exception.SendCodeException;
import com.example.bookshopapp.exception.SendSMSException;
import com.example.bookshopapp.service.AuthService;
import com.example.bookshopapp.service.LoginService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.annotations.Api;
import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping("/api")
@Api("auth REST controller")
public class AuthRestApiController {
    private final AuthService authService;
    private final LoginService loginService;

    @Autowired
    public AuthRestApiController(AuthService authService, LoginService loginService) {
        this.authService = authService;
        this.loginService = loginService;
    }

    @PostMapping("/requestContactConfirmation")
    public ResponseEntity<ApiResponse<ResultResponse>> handleRequestContactConfirmation(
            @RequestBody ContactConfirmationPayload payload) throws NoSuchAlgorithmException,
            JsonProcessingException, SendCodeException, NotFoundException, SendSMSException {
        if (payload.getContact() != null && !payload.getContact().isEmpty()) {
            authService.registerContactConfirmation(payload.getContact());
        }
        return getResultResponseStatus200(new ResultResponse(true));
    }

    @PostMapping("/requestLoginContactConfirmation")
    public ResponseEntity<ApiResponse<ResultResponse>> handleRequestLoginContactConfirmation(
            @RequestBody ContactConfirmationPayload payload) throws NoSuchAlgorithmException,
            JsonProcessingException, SendCodeException, SendSMSException {
        if (payload.getContact() != null && !payload.getContact().isEmpty()) {
            authService.loginContactConfirmation(payload.getContact());
        }
        return getResultResponseStatus200(new ResultResponse(true));
    }

    /**
     * Метод авторизации и проверки пароля
     * Если код одобрен и пользователь зарегистрирован (id пользователя не равен null и не равен SERVICE_USER_ID)
     * происходит авторизация. В противном случае происходит подтверждение контакта.
     */
    @PostMapping("/approveContact")
    public ResponseEntity<ApiResponse<ResultResponse>> handleApproveContact(
            @RequestBody ContactConfirmationPayload payload, HttpServletResponse response) throws CheckCodeException {
        Integer userId = authService.approveContact(payload.getContact(), payload.getCode());
        if (userId != null && userId != BookShopConfig.SERVICE_USER_ID) {
            String token = loginService.jwtLogin(payload);
            ResponseCookie cookie = ResponseCookie.from("token", token)
                    .httpOnly(true).secure(true).path("/").sameSite("Lax").build();
            response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        }
        return getResultResponseStatus200(new ResultResponse(true));
    }

    private ResponseEntity<ApiResponse<ResultResponse>> getResultResponseStatus200(ResultResponse data) {
        ApiResponse<ResultResponse> response = new ApiResponse<>();
        response.setDebugMessage("successful request");
        response.setStatus(HttpStatus.OK);
        response.setData(data);
        return ResponseEntity.ok(response);
    }
}
