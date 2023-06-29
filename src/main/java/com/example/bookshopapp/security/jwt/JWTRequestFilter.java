package com.example.bookshopapp.security.jwt;

import com.example.bookshopapp.config.BookShopConfig;
import com.example.bookshopapp.security.code.BookShopUserDetails;
import com.example.bookshopapp.security.code.BookShopUserDetailsService;
import com.example.bookshopapp.security.code.UserContactAuthenticationToken;
import com.example.bookshopapp.service.BookStatusService;
import com.example.bookshopapp.service.CookieService;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Slf4j
public class JWTRequestFilter extends OncePerRequestFilter {

    private final BookShopUserDetailsService bookShopUserDetailsService;
    private final JWTUtil jwtUtil;
    private final BookStatusService bookStatusService;

    @Autowired
    public JWTRequestFilter(BookShopUserDetailsService bookShopUserDetailsService, JWTUtil jwtUtil,
                            BookStatusService bookStatusService) {
        this.bookShopUserDetailsService = bookShopUserDetailsService;
        this.jwtUtil = jwtUtil;
        this.bookStatusService = bookStatusService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest,
                                    HttpServletResponse httpServletResponse,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = null;
        String username = null;
        Cookie[] cookies = httpServletRequest.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(BookShopConfig.TOKEN_COOKIE_NAME)) {
                    token = cookie.getValue();
                    try{
                        username = jwtUtil.extractUsername(token);
                    } catch (ExpiredJwtException ex){
                        CookieService.deleteCookieByName(httpServletRequest, BookShopConfig.TOKEN_COOKIE_NAME);
                        continue;
                    }
                }

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    BookShopUserDetails userDetails =
                            (BookShopUserDetails) bookShopUserDetailsService.loadUserByUsername(username);
                    if (Boolean.TRUE.equals(jwtUtil.validateToken(token, userDetails))) {
                        UserContactAuthenticationToken authenticationToken =
                                new UserContactAuthenticationToken(
                                        userDetails, userDetails.getAuthorities());
                        authenticationToken.setDetails(new WebAuthenticationDetailsSource()
                                .buildDetails(httpServletRequest));
                        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                        bookStatusService.movePostponedAndCartFromCookieToDB(httpServletRequest, httpServletResponse);
                    }
                }
            }
        }
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }
}