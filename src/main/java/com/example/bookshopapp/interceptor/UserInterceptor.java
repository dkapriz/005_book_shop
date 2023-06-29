package com.example.bookshopapp.interceptor;

import com.example.bookshopapp.model.User;
import com.example.bookshopapp.service.AuthService;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserInterceptor implements HandlerInterceptor {
    private final AuthService authService;

    public UserInterceptor(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) {
        if (modelAndView == null) {
            return;
        }

        User user = authService.getCurrentUser();
        if (user == null) {
            modelAndView.addObject("status", "unauthorized");
        } else {
            modelAndView.addObject("status", "authorized");
            modelAndView.addObject("username", user.getName());
            modelAndView.addObject("userHash", user.getHash());
            modelAndView.addObject("balance", user.getBalance());
        }
    }
}
