package com.example.bookshopapp.service;

import com.example.bookshopapp.model.Book;
import com.example.bookshopapp.model.BookEvaluation;
import com.example.bookshopapp.repositories.Book2UserRepository;
import com.example.bookshopapp.repositories.BookEvaluationRepository;
import com.example.bookshopapp.repositories.BookRepository;
import org.junit.jupiter.api.AfterEach;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@SpringBootTest
class BooksRatingAndPopularityServiceTest {
    private static final int TEST_BOOK_ID = 1;
    @MockBean
    private Book2UserRepository book2UserRepositoryMock;
    @MockBean
    private BookRepository bookRepositoryMock;
    @MockBean
    private BookEvaluationRepository bookEvaluationRepositoryMock;
    private final BooksRatingAndPopularityService booksRatingAndPopularityService;
    private Book testBook;

    @Autowired
    BooksRatingAndPopularityServiceTest(BooksRatingAndPopularityService booksRatingAndPopularityService) {
        this.booksRatingAndPopularityService = booksRatingAndPopularityService;
    }

    @BeforeEach
    void setUp() {
        testBook = new Book();
        testBook.setId(TEST_BOOK_ID);
        testBook.setRating(0.0);
        testBook.setPopularIndex(0.0);

        when(bookRepositoryMock.save(Mockito.any(Book.class)))
                .thenAnswer(invocationOnMock -> invocationOnMock.getArguments()[0]);
    }

    @AfterEach
    void tearDown() {
        testBook = null;
    }

    @Test
    void updateBookRating() {
        List<BookEvaluation> bookEvaluationList = new ArrayList<>();
        byte value = 1;
        for (int i = 1; i <= 50; ++i) {
            BookEvaluation bookEvaluation = new BookEvaluation();
            bookEvaluation.setId(i);
            bookEvaluation.setValue(value);
            bookEvaluationList.add(bookEvaluation);
            value++;
            if(value == 6){
                value = 1;
            }
        }
        when(bookEvaluationRepositoryMock.findAllByBookAndValueNot(Mockito.any(Book.class), Mockito.anyByte()))
                .thenReturn(bookEvaluationList);
        Book book = booksRatingAndPopularityService.updateBookRating(testBook);

        assertNotNull(book);
        assertEquals(3.0, book.getRating());
        Mockito.verify(bookEvaluationRepositoryMock, times(1))
                .findAllByBookAndValueNot(Mockito.any(Book.class), Mockito.anyByte());
        Mockito.verify(bookRepositoryMock, times(1)).save(Mockito.any(Book.class));
    }

    @Test
    void updateBookRatingNotFound() {
        Book book = booksRatingAndPopularityService.updateBookRating(testBook);

        assertNotNull(book);
        assertEquals(0.0, book.getRating());
        Mockito.verify(bookEvaluationRepositoryMock, times(1))
                .findAllByBookAndValueNot(Mockito.any(Book.class), Mockito.anyByte());
        Mockito.verify(bookRepositoryMock, times(0)).save(Mockito.any(Book.class));
    }

    @Test
    void updateBookPopularIndex() {
        when(book2UserRepositoryMock
                .countBooksByStatusAndBookId(Mockito.anyString(), eq(TEST_BOOK_ID))).thenReturn(10);
        Book book = booksRatingAndPopularityService.updateBookPopularIndex(testBook);

        assertNotNull(book);
        assertEquals(21.0, book.getPopularIndex());
        Mockito.verify(book2UserRepositoryMock, times(3))
                .countBooksByStatusAndBookId(Mockito.anyString(), Mockito.anyInt());
        Mockito.verify(bookRepositoryMock, times(1)).save(Mockito.any(Book.class));
    }

    @Test
    void updateBookPopularIndexNotFound() {
        Book book = booksRatingAndPopularityService.updateBookPopularIndex(testBook);

        assertNotNull(book);
        assertEquals(0.0, book.getPopularIndex());
        Mockito.verify(book2UserRepositoryMock, times(3))
                .countBooksByStatusAndBookId(Mockito.anyString(), Mockito.anyInt());
        Mockito.verify(bookRepositoryMock, times(0)).save(Mockito.any(Book.class));
    }
}