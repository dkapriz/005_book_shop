package com.example.bookshopapp.controllers;

import com.example.bookshopapp.api.request.StringRequest;
import com.example.bookshopapp.api.response.BookListResponse;
import com.example.bookshopapp.config.BookShopConfig;
import com.example.bookshopapp.exception.ViewNotFoundParameterException;
import com.example.bookshopapp.model.Author;
import com.example.bookshopapp.service.AuthorService;
import com.example.bookshopapp.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

import static com.example.bookshopapp.config.LanguageMessage.EX_MSG_EMPTY_PARAM;

@Controller
public class AuthorsController {
    private final AuthorService authorService;
    private final BookService bookService;
    private final BookShopConfig bsConfig;

    @Autowired
    public AuthorsController(AuthorService authorService, BookService bookService, BookShopConfig bsConfig) {
        this.authorService = authorService;
        this.bookService = bookService;
        this.bsConfig = bsConfig;
    }

    @ModelAttribute("authorsMap")
    public Map<String, List<Author>> authorsMap() {
        return authorService.getAuthorsMap();
    }

    @ModelAttribute("letterEn")
    public Map<String, String> letterEn() {
        return authorService.getLetterMapEn();
    }

    @GetMapping("/authors")
    public String authorsPage() {
        return "/authors/index";
    }

    @GetMapping("/authors/{slug}")
    public String authorPage(@PathVariable(value = "slug", required = false) StringRequest slugRequest, Model model)
            throws ViewNotFoundParameterException {
        if (slugRequest != null) {
            Author author = authorService.getAuthorBySlug(slugRequest.getStr());
            BookListResponse bookListResponse = bookService.getPageOfBooksByAuthorSlug(0,
                    bsConfig.getThPageBookShowLimit(), slugRequest.getStr());
            model.addAttribute("author", author);
            model.addAttribute("parseDescription", authorService.getAuthorParseDescription(author.getDescription()));
            model.addAttribute("bookListResult", bookListResponse);
            return "/authors/slug";
        }
        throw new ViewNotFoundParameterException(EX_MSG_EMPTY_PARAM);
    }

    @GetMapping(value = {"/books/author", "/books/author/{slug}"})
    public String authorBookPageBySlug(@PathVariable(value = "slug", required = false) StringRequest slugRequest,
                                       Model model) throws ViewNotFoundParameterException {
        if (slugRequest != null) {
            Author author = authorService.getAuthorBySlug(slugRequest.getStr());
            BookListResponse bookListResponse = bookService.getPageOfBooksByAuthorSlug(0,
                    bsConfig.getThPageBookShowLimit(), slugRequest.getStr());
            model.addAttribute("author", author);
            model.addAttribute("bookListResult", bookListResponse);
            return "/books/author";
        }
        throw new ViewNotFoundParameterException(EX_MSG_EMPTY_PARAM);
    }
}
