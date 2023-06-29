package com.example.bookshopapp.service;

import com.example.bookshopapp.api.request.ContactConfirmationPayload;
import com.example.bookshopapp.api.response.ResultResponse;
import com.example.bookshopapp.security.code.BookShopUserDetails;
import com.example.bookshopapp.security.code.BookShopUserDetailsService;
import com.example.bookshopapp.security.code.UserContactAuthenticationToken;
import com.example.bookshopapp.security.jwt.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class LoginService {
    private final AuthenticationManager authenticationManager;
    private final BookShopUserDetailsService bookShopUserDetailsService;
    private final JWTUtil jwtUtil;

    @Autowired
    public LoginService(AuthenticationManager authenticationManager,
                        BookShopUserDetailsService bookShopUserDetailsService, JWTUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.bookShopUserDetailsService = bookShopUserDetailsService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Метод базовой аутентификации пользователя на основе UsernamePasswordAuthentication
     *
     * @return ResultResponse value
     */
    public ResultResponse basicLogin(ContactConfirmationPayload payload) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(payload.getContact(), payload.getCode()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return new ResultResponse(true);
    }

    /**
     * Метод аутентификации пользователя на основе JWT и UserContactAuthentication
     *
     * @return JWT токен
     */
    public String jwtLogin(ContactConfirmationPayload payload) {
        authenticationManager.authenticate(new UserContactAuthenticationToken(payload.getContact()));
        BookShopUserDetails userDetails =
                (BookShopUserDetails) bookShopUserDetailsService.loadUserByUsername(payload.getContact());
        return jwtUtil.generateToken(userDetails);
    }
}
