package com.example.bookshopapp.security.jwt;

import com.example.bookshopapp.config.BookShopConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class JWTLogoutHandler implements LogoutHandler {

    private final JWTUtil jwtUtil;

    @Autowired
    public JWTLogoutHandler(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void logout(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                       Authentication authentication) {
        Cookie[] cookies = httpServletRequest.getCookies();
        if (cookies == null) {
            return;
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(BookShopConfig.TOKEN_COOKIE_NAME)) {
                jwtUtil.addToBlackList(cookie.getValue());
            }
        }
    }
}
