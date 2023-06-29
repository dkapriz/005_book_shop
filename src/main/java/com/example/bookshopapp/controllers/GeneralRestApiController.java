package com.example.bookshopapp.controllers;

import com.example.bookshopapp.api.request.BookRateRequest;
import com.example.bookshopapp.api.request.BookReviewRateRequest;
import com.example.bookshopapp.api.request.BookReviewRequest;
import com.example.bookshopapp.api.request.ChangeBookStatusRequest;
import com.example.bookshopapp.api.response.ApiResponse;
import com.example.bookshopapp.api.response.ResultResponse;
import com.example.bookshopapp.exception.WrongParameterException;
import com.example.bookshopapp.service.BookStatusService;
import com.example.bookshopapp.service.BooksRatingAndPopularityService;
import com.example.bookshopapp.service.ReviewService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api")
@Api("general REST controller")
public class GeneralRestApiController {
    private final BooksRatingAndPopularityService booksRatingAndPopularityService;
    private final BookStatusService bookStatusService;
    private final ReviewService reviewService;

    @Autowired
    public GeneralRestApiController(BooksRatingAndPopularityService booksRatingAndPopularityService,
                                    BookStatusService bookStatusService, ReviewService reviewService) {
        this.booksRatingAndPopularityService = booksRatingAndPopularityService;
        this.bookStatusService = bookStatusService;
        this.reviewService = reviewService;
    }

    @PostMapping("/rateBook")
    @ApiOperation("adding a book estimation")
    public ResponseEntity<ApiResponse<ResultResponse>> setBookEstimate(
            @RequestBody BookRateRequest bookRateRequest,
            @CookieValue(name = "bookRateContents", required = false) String bookRateContents,
            HttpServletResponse response)
            throws WrongParameterException {
        return getResultResponseStatus200(new ResultResponse(booksRatingAndPopularityService
                .setBookRating(bookRateRequest.getBookId(), bookRateRequest.getValue(),
                        bookRateContents, response) != null));
    }

    @PostMapping("/changeBookStatus")
    @ApiOperation("change status book")
    public ResponseEntity<ApiResponse<ResultResponse>> handleChangeBookStatus(
            @RequestBody ChangeBookStatusRequest changeBookStatusRequest,
            @CookieValue(name = "cartContents", required = false) String cartContents,
            @CookieValue(name = "postponedContents", required = false) String postponedContents,
            HttpServletResponse response) throws WrongParameterException {
        bookStatusService.handleChangeBookStatus(changeBookStatusRequest.getBooksIds(),
                changeBookStatusRequest.getStatus(), cartContents, postponedContents, response);
        return getResultResponseStatus200(new ResultResponse(true));
    }

    @PostMapping("/rateBookReview")
    @PreAuthorize("hasRole('ROLE_USER')")
    @ApiOperation("Add a book review like")
    public ResponseEntity<ApiResponse<ResultResponse>> setBookReviewRate(
            @RequestBody BookReviewRateRequest bookReviewRateRequest,
            @CookieValue(name = "rateBookReviewHash", required = false) String rateBookReviewHashContents,
            HttpServletResponse response) throws WrongParameterException {
        return getResultResponseStatus200(new ResultResponse(reviewService
                .addBookReviewRate(bookReviewRateRequest.getReviewId(),
                        bookReviewRateRequest.getValue(), rateBookReviewHashContents, response)));
    }

    @PostMapping("/bookReview")
    @PreAuthorize("hasRole('ROLE_USER')")
    @ApiOperation("Add a book review")
    public ResponseEntity<ApiResponse<ResultResponse>> addBookReview(@RequestBody BookReviewRequest bookReviewRequest)
            throws WrongParameterException {
        return getResultResponseStatus200(new ResultResponse(reviewService
                .addBookReview(bookReviewRequest.getBookId(), bookReviewRequest.getText())));
    }

    private ResponseEntity<ApiResponse<ResultResponse>> getResultResponseStatus200(ResultResponse data) {
        ApiResponse<ResultResponse> response = new ApiResponse<>();
        response.setDebugMessage("successful request");
        response.setStatus(HttpStatus.OK);
        response.setData(data);
        return ResponseEntity.ok(response);
    }
}