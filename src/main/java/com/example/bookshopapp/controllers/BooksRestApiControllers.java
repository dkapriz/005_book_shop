package com.example.bookshopapp.controllers;

import com.example.bookshopapp.api.request.SearchWordRequest;
import com.example.bookshopapp.api.response.ApiResponse;
import com.example.bookshopapp.api.response.BookListResponse;
import com.example.bookshopapp.service.BookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Api("book REST controller")
public class BooksRestApiControllers {
    private final BookService bookService;

    @Autowired
    public BooksRestApiControllers(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/books/recommended")
    @ApiOperation("getting a list of recommended books")
    public ResponseEntity<ApiResponse<BookListResponse>> getRecommendedBooks(
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "20") Integer limit) {
        return getBookListResponseStatus200(bookService.getPageOfRecommendedBooks(offset, limit));
    }

    @GetMapping("/books/recent")
    @ApiOperation("getting a list of recent books")
    public ResponseEntity<ApiResponse<BookListResponse>> getRecentBooks(
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(defaultValue = "") String from,
            @RequestParam(defaultValue = "") String to) {
        return getBookListResponseStatus200(bookService.getPageOfRecentBooks(offset, limit, from, to));
    }

    @GetMapping("/books/popular")
    @ApiOperation("getting a list of popular books")
    public ResponseEntity<ApiResponse<BookListResponse>> getPopularBooks(
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "20") Integer limit) {
        return getBookListResponseStatus200(bookService.getPageOfPopularBooks(offset, limit));
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/books/viewed")
    @ApiOperation("getting a list of viewed books")
    public ResponseEntity<ApiResponse<BookListResponse>> getViewedBooks(
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "20") Integer limit) {
        return getBookListResponseStatus200(bookService.getPageOfViewedBooks(offset, limit));
    }

    @GetMapping("/search/{searchWord}")
    @ApiOperation("getting a list of books by query string (search)")
    public ResponseEntity<ApiResponse<BookListResponse>> getSearchBooks(
            @PathVariable(value = "searchWord", required = false)
            SearchWordRequest searchWordRequest,
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "20") Integer limit) {
        return getBookListResponseStatus200(bookService.getPageOfSearchResultBook(
                offset, limit, searchWordRequest.getStr()));
    }

    @GetMapping("/books/tag/{id}")
    @ApiOperation("getting a list of books by tag")
    public ResponseEntity<ApiResponse<BookListResponse>> getBooksByTag(
            @PathVariable(value = "id", required = false) Integer id,
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "20") Integer limit) {
        return getBookListResponseStatus200(bookService.getPageOfBooksByTagId(offset, limit, id));
    }

    @GetMapping("/books/genre/{id}")
    @ApiOperation("getting a list of books by genre")
    public ResponseEntity<ApiResponse<BookListResponse>> getBooksByGenre(
            @PathVariable(value = "id", required = false) Integer id,
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "20") Integer limit) {
        return getBookListResponseStatus200(bookService.getPageOfBooksByGenreId(offset, limit, id));
    }

    @GetMapping("/books/author/{id}")
    @ApiOperation("getting a list of books by author")
    public ResponseEntity<ApiResponse<BookListResponse>> getBooksByAuthor(
            @PathVariable(value = "id", required = false) Integer id,
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "20") Integer limit) {
        return getBookListResponseStatus200(bookService.getPageOfBooksByAuthorId(offset, limit, id));
    }

    private ResponseEntity<ApiResponse<BookListResponse>> getBookListResponseStatus200(BookListResponse data) {
        ApiResponse<BookListResponse> response = new ApiResponse<>();
        response.setDebugMessage("successful request");
        response.setMessage("data size: " + data.getBooks().size() + " elements");
        response.setStatus(HttpStatus.OK);
        response.setData(data);
        return ResponseEntity.ok(response);
    }
}