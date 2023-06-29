package com.example.bookshopapp.interceptor;

import com.example.bookshopapp.api.dto.BookDto;
import com.example.bookshopapp.api.request.SearchWordRequest;
import com.example.bookshopapp.config.BookShopConfig;
import com.example.bookshopapp.model.User;
import com.example.bookshopapp.model.enums.BookStatus;
import com.example.bookshopapp.service.AuthService;
import com.example.bookshopapp.service.BookStatusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Slf4j
public class HeaderInterceptor implements HandlerInterceptor {
    private final BookStatusService bookStatusService;
    private final AuthService authService;
    private final BookShopConfig config;

    public HeaderInterceptor(BookStatusService bookStatusService, AuthService authService, BookShopConfig config) {
        this.bookStatusService = bookStatusService;
        this.authService = authService;
        this.config = config;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, ModelAndView modelAndView) {
        if (modelAndView == null) {
            return;
        }
        User user = authService.getCurrentUser();
        if (user == null) {
            modelAndView.addObject("countCartBooks", getCountBooksFromCookie(
                    request, BookShopConfig.CART_COOKIE_NAME));
            modelAndView.addObject("countPostponedBooks", getCountBooksFromCookie(
                    request, BookShopConfig.POSTPONED_COOKIE_NAME));
        } else {
            modelAndView.addObject("countCartBooks",
                    bookStatusService.getCountBooksFromDataBase(user, BookStatus.CART));
            modelAndView.addObject("countPostponedBooks",
                    bookStatusService.getCountBooksFromDataBase(user, BookStatus.KEPT));
            Integer countPaidBooks = bookStatusService.getCountBooksFromDataBase(user, BookStatus.PAID);
            Integer countArchivedBooks = bookStatusService.getCountBooksFromDataBase(user, BookStatus.ARCHIVED);
            modelAndView.addObject("countPaidBooks", countPaidBooks + countArchivedBooks);
        }
        modelAndView.addObject("searchWordDto", new SearchWordRequest());
        modelAndView.addObject("shopName", config.getShopName());
    }

    private Integer getCountBooksFromCookie(HttpServletRequest request, String nameCookie) {
        if (request.getCookies() == null || request.getCookies().length == 0) {
            return 0;
        }
        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals(nameCookie)) {
                List<BookDto> bookCart = bookStatusService.getBooksFromCookie(cookie.getValue());
                return bookCart.size();
            }
        }
        return 0;
    }
}