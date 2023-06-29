package com.example.bookshopapp.controllers;

import com.example.bookshopapp.api.dto.BookDto;
import com.example.bookshopapp.api.dto.RegistrationForm;
import com.example.bookshopapp.model.enums.BookStatus;
import com.example.bookshopapp.service.AuthService;
import com.example.bookshopapp.service.BookService;
import javassist.tools.reflect.CannotCreateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class AuthController {
    private final AuthService authService;
    private final BookService bookService;

    @Autowired
    public AuthController(AuthService authService, BookService bookService) {
        this.authService = authService;
        this.bookService = bookService;
    }

    @ModelAttribute("bookListUserPaid")
    public List<BookDto> bookListUserPaid() {
        return bookService.getBookListDtoUserByStatus(BookStatus.PAID);
    }

    @ModelAttribute("bookListUserArchived")
    public List<BookDto> bookListUserArchived() {
        return bookService.getBookListDtoUserByStatus(BookStatus.ARCHIVED);
    }

    @GetMapping("/signin")
    public String signInPage() {
        return "/signin";
    }

    @GetMapping("/signup")
    public String signUpPage(Model model) {
        model.addAttribute("regForm", new RegistrationForm());
        return "/signup";
    }

    @PostMapping("/reg")
    public String handleUserRegistration(RegistrationForm registrationForm, Model model) throws CannotCreateException {
        authService.registerNewUser(registrationForm);
        model.addAttribute("regOk", true);
        return "signin";
    }

    @GetMapping("/my")
    public String handleMy() {
        return "my";
    }

    @GetMapping("/my/archive")
    public String handleMyArchive() {
        return "myarchive";
    }
}