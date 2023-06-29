package com.example.bookshopapp.service;

import com.example.bookshopapp.config.BookShopConfig;
import com.example.bookshopapp.model.BookReview;
import com.example.bookshopapp.model.BookReviewLike;
import com.example.bookshopapp.repositories.BookReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@SpringBootTest
class ReviewServiceTest {

    @MockBean
    private BookReviewRepository bookReviewRepositoryMock;
    private final ReviewService reviewService;

    @Autowired
    ReviewServiceTest(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @BeforeEach
    void setUp() {
        when(bookReviewRepositoryMock.save(Mockito.any(BookReview.class)))
                .thenAnswer(invocationOnMock -> invocationOnMock.getArguments()[0]);
    }

    @Test
    void updateReviewRating() {
        List<BookReviewLike> bookReviewLikeList = new ArrayList<>();
        for (int i = 1; i <= 1000; ++i) {
            BookReviewLike bookReviewLike = new BookReviewLike();
            bookReviewLike.setId(i);
            if (i > 800) {
                bookReviewLike.setValue(BookShopConfig.REVIEW_DISLIKE);
            } else {
                bookReviewLike.setValue(BookShopConfig.REVIEW_LIKE);
            }
            bookReviewLikeList.add(bookReviewLike);
        }
        BookReview bookReview = new BookReview();
        bookReview.setRating(0);
        bookReview.setBookReviewLikes(bookReviewLikeList);
        bookReview = reviewService.updateReviewRating(bookReview);

        assertNotNull(bookReview);
        assertEquals(600, bookReview.getRating());
        Mockito.verify(bookReviewRepositoryMock, times(1)).save(Mockito.any(BookReview.class));
    }

    @Test
    void updateReviewRatingNotValues() {
        BookReview bookReview = new BookReview();
        bookReview.setRating(0);
        bookReview = reviewService.updateReviewRating(bookReview);

        assertNotNull(bookReview);
        assertEquals(0, bookReview.getRating());
        Mockito.verify(bookReviewRepositoryMock, times(1)).save(Mockito.any(BookReview.class));
    }
}