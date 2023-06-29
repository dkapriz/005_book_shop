package com.example.bookshopapp.controllers;

import com.example.bookshopapp.api.dto.BookDto;
import com.example.bookshopapp.api.dto.TagDto;
import com.example.bookshopapp.api.request.SearchWordRequest;
import com.example.bookshopapp.api.response.BookListResponse;
import com.example.bookshopapp.config.BookShopConfig;
import com.example.bookshopapp.exception.ViewEmptyParameterException;
import com.example.bookshopapp.service.BookService;
import com.example.bookshopapp.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.List;

import static com.example.bookshopapp.config.LanguageMessage.EX_MSG_EMPTY_PARAM;

@Controller
public class MainPageController {
    private final BookService bookService;
    private final TagService tagService;
    private final BookShopConfig bsConfig;

    @Autowired
    public MainPageController(BookService bookService, TagService tagService,
                              BookShopConfig bsConfig) {
        this.bookService = bookService;
        this.tagService = tagService;
        this.bsConfig = bsConfig;
    }

    @ModelAttribute("recommendedBookList")
    public List<BookDto> recommendedBookList() {
        return bookService.getPageOfRecommendedBooks(0, bsConfig.getThPageBookShowLimit()).getBooks();
    }

    @ModelAttribute("recentBookList")
    public List<BookDto> recentBookList() {
        return bookService.getPageOfRecentBooks(0, bsConfig.getThPageBookShowLimit(), "", "").getBooks();
    }

    @ModelAttribute("popularBookList")
    public List<BookDto> popularBookList() {
        return bookService.getPageOfPopularBooks(0, bsConfig.getThPageBookShowLimit()).getBooks();
    }

    @ModelAttribute("searchResult")
    public BookListResponse searchResult() {
        return new BookListResponse(new ArrayList<>());
    }

    @ModelAttribute("tagList")
    public List<TagDto> tagList() {
        return tagService.getTags();
    }

    @GetMapping("/")
    public String mainPage() {
        return "index";
    }

    @GetMapping(value = {"/search", "/search/{searchWord}"})
    public String getSearchResult(@PathVariable(value = "searchWord", required = false)
                                  SearchWordRequest searchWordRequest, Model model)
            throws ViewEmptyParameterException {
        if (searchWordRequest != null) {
            BookListResponse bookListResponse = bookService.getPageOfSearchResultBook(
                    0, bsConfig.getThPageBookShowLimit(), searchWordRequest.getStr());
            model.addAttribute("searchWordDto", searchWordRequest);
            model.addAttribute("searchResult", bookListResponse);
            return "/search/index";
        }
        throw new ViewEmptyParameterException(EX_MSG_EMPTY_PARAM);
    }
}
