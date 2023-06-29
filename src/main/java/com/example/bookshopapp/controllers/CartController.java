package com.example.bookshopapp.controllers;

import com.example.bookshopapp.api.dto.BookDto;
import com.example.bookshopapp.exception.PaymentException;
import com.example.bookshopapp.model.User;
import com.example.bookshopapp.model.enums.BookStatus;
import com.example.bookshopapp.service.AuthService;
import com.example.bookshopapp.service.BookService;
import com.example.bookshopapp.service.BookStatusService;
import com.example.bookshopapp.service.TransactionalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.web.context.WebApplicationContext.SCOPE_SESSION;


@Controller
@Scope(value = SCOPE_SESSION)
public class CartController {
    private final BookService bookService;    private final BookStatusService bookStatusService;
    private final AuthService authService;
    private final TransactionalService transactionalService;

    @Autowired
    public CartController(BookService bookService, BookStatusService bookStatusService, AuthService authService,
                          TransactionalService transactionalService) {
        this.bookStatusService = bookStatusService;
        this.bookService = bookService;
        this.authService = authService;
        this.transactionalService = transactionalService;
    }

    @ModelAttribute(name = "bookCart")
    public List<BookDto> bookCard() {
        return new ArrayList<>();
    }

    @ModelAttribute(name = "totalPrice")
    public Integer totalPrice() {
        return 0;
    }

    @ModelAttribute(name = "totalDiscountPrice")
    public Integer totalDiscountPrice() {
        return 0;
    }

    @ModelAttribute(name = "booksIds")
    public String booksIds() {
        return "";
    }

    @GetMapping("/cart")
    public String handleCartRequest(@CookieValue(value = "cartContents", required = false) String cartContents,
                                    Model model) {
        User user = authService.getCurrentUser();
        if (user == null) {
            addModelBooksAttrFromCookie(cartContents, "countCartBooks", model);
        } else {
            addModelBooksAttrFromDB(BookStatus.CART, user, "countCartBooks", model);
        }
        return "cart";
    }

    @GetMapping("/postponed")
    public String handlePostponedRequest(
            @CookieValue(value = "postponedContents", required = false) String postponedContents, Model model) {
        User user = authService.getCurrentUser();
        if (user == null) {
            addModelBooksAttrFromCookie(postponedContents, "countPostponedBooks", model);
        } else {
            addModelBooksAttrFromDB(BookStatus.KEPT, user, "countPostponedBooks", model);
        }
        return "postponed";
    }

    @GetMapping("/order")
    public String handleOrder() throws PaymentException {
        String uri = transactionalService.handleCartPaid();
        if (uri.isEmpty()) {
            return "redirect:cart";
        }
        return "redirect:" + uri;
    }

    private void addModelBooksAttrFromCookie(String contents, String countBookAttrName, Model model) {
        if (contents != null && !contents.isEmpty()) {
            List<BookDto> bookCart = bookStatusService.getBooksFromCookie(contents);
            addModelBooksAttr(bookCart, countBookAttrName, model);
        }
    }

    private void addModelBooksAttrFromDB(BookStatus status, User user, String countBookAttrName, Model model) {
        List<BookDto> bookCart = bookStatusService.getBooksDtoByStatus(status, user);
        addModelBooksAttr(bookCart, countBookAttrName, model);
    }

    private void addModelBooksAttr(List<BookDto> bookCart, String countBookAttrName, Model model) {
        String bookIds = "[" + bookCart.stream().map(bookDto -> bookDto.getId().toString())
                .collect(Collectors.joining(", ")) + "]";
        model.addAttribute("bookCart", bookCart);
        model.addAttribute("booksIds", bookIds);
        model.addAttribute(countBookAttrName, bookCart.size());
        model.addAttribute("totalPrice", bookService.getTotalPrice(bookCart));
        model.addAttribute("totalDiscountPrice", bookService.getTotalDiscountPrice(bookCart));
    }
}