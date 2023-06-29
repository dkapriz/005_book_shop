package com.example.bookshopapp.service;

import com.example.bookshopapp.api.dto.DistributionRating;
import com.example.bookshopapp.config.BookShopConfig;
import com.example.bookshopapp.config.LanguageMessage;
import com.example.bookshopapp.exception.BookListWrongParameterException;
import com.example.bookshopapp.exception.WrongParameterException;
import com.example.bookshopapp.model.*;
import com.example.bookshopapp.model.compositekey.BookUserId;
import com.example.bookshopapp.model.enums.BookStatus;
import com.example.bookshopapp.repositories.Book2UserRecentlyViewRepository;
import com.example.bookshopapp.repositories.Book2UserRepository;
import com.example.bookshopapp.repositories.BookEvaluationRepository;
import com.example.bookshopapp.repositories.BookRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.*;

import static com.example.bookshopapp.config.BookShopConfig.DAYS_EVALUATION_VIEWS;

@Service
@Slf4j
public class BooksRatingAndPopularityService {
    private final BookRepository bookRepository;
    private final Book2UserRepository book2UserRepository;
    private final BookEvaluationRepository bookEvaluationRepository;
    private final Book2UserRecentlyViewRepository book2UserRecentlyViewRepository;
    private final CookieService cookieService;
    private final AuthService authService;

    @Autowired
    public BooksRatingAndPopularityService(BookRepository bookRepository, Book2UserRepository book2UserRepository,
                                           BookEvaluationRepository bookEvaluationRepository,
                                           Book2UserRecentlyViewRepository book2UserRecentlyViewRepository,
                                           CookieService cookieService, AuthService authService) {
        this.bookRepository = bookRepository;
        this.book2UserRepository = book2UserRepository;
        this.bookEvaluationRepository = bookEvaluationRepository;
        this.book2UserRecentlyViewRepository = book2UserRecentlyViewRepository;
        this.cookieService = cookieService;
        this.authService = authService;
    }

    @Scheduled(cron = BookShopConfig.BOOK_UPDATE_FREQUENCY)
    public void updateBooksRating() {
        bookRepository.findAll().forEach(this::updateBookRating);
        log.info("Update book rating");
    }

    protected Book updateBookRating(Book book) {
        double rating = 0;
        byte value = 0;
        List<BookEvaluation> bookEvaluations = bookEvaluationRepository.findAllByBookAndValueNot(book, value);
        if (!bookEvaluations.isEmpty()) {
            double sum = 0;
            for (BookEvaluation bookEvaluation : bookEvaluations) {
                sum += bookEvaluation.getValue();
            }
            rating = sum / bookEvaluations.size();
        }
        if (rating == book.getRating()) {
            return book;
        }
        book.setRating(rating);
        return bookRepository.save(book);
    }

    @Scheduled(cron = BookShopConfig.BOOK_UPDATE_FREQUENCY)
    public void updateBooksPopularIndex() {
        bookRepository.findAll().forEach(this::updateBookPopularIndex);
        log.info("Update book popular index");
    }

    /**
     * Метод расчета индекса популярности книги. В расчете учитываются купленные, добавленные пользователем в
     * корзину и отложенные книги, а так же просмотренные книги
     */
    protected Book updateBookPopularIndex(Book book) {
        Integer countPaid = book2UserRepository.countBooksByStatusAndBookId(BookStatus.PAID.getStatus(), book.getId());
        Integer countCart = book2UserRepository.countBooksByStatusAndBookId(BookStatus.CART.getStatus(), book.getId());
        Integer countKept = book2UserRepository.countBooksByStatusAndBookId(BookStatus.KEPT.getStatus(), book.getId());
        Integer countViewed = book2UserRecentlyViewRepository
                .countAllByBookAndTimeAfter(book, LocalDateTime.now().minusDays(DAYS_EVALUATION_VIEWS));
        Double popularIndex = countPaid + 0.7 * countCart + 0.4 * countKept + 0.3 * countViewed;
        if (popularIndex.equals(book.getPopularIndex())) {
            return book;
        }
        book.setPopularIndex(popularIndex);
        return bookRepository.save(book);
    }

    public DistributionRating getDistributionRatingBook(Integer bookId) throws BookListWrongParameterException {
        Optional<Book> book = bookRepository.findById(bookId);
        if (!book.isPresent()) {
            log.warn("getDistributionRatingBook (handling null value) bookId:" + bookId);
            throw new BookListWrongParameterException(LanguageMessage.EX_MSG_MISSING_RESULT);
        }
        Map<Integer, Integer> distributionRating = new HashMap<>();
        int countRating = 0;
        for (byte i = 1; i <= 5; i++) {
            int currentRating = bookEvaluationRepository.findAllByBookAndValue(book.get(), i).size();
            countRating += currentRating;
            distributionRating.put((int) i, currentRating);
        }
        return new DistributionRating(distributionRating, countRating);
    }

    /**
     * Установка рейтинга книги (оценка книги пользователем)
     * Если пользователь в системе, то рейтинг книги обновляется, в противном случае создается анонимный рейтинг
     * Анонимный рейтинг сохраняется в Cookie под именем bookRateContents
     * Формат ячейки Cookie: "bookId-bookEvaluationId"
     * Если в Cookie найдена книга, то рейтинг обновляется по сохраненному в Cookie bookEvaluationId
     */
    public BookEvaluation setBookRating(Integer bookId, Byte value, String bookRateContents,
                                        HttpServletResponse response) throws WrongParameterException {
        Optional<Book> bookOpt = bookRepository.findById(bookId);
        if (!bookOpt.isPresent()) {
            log.warn("setBookRating (handling null value) bookId:" + bookId);
            throw new WrongParameterException(LanguageMessage.EX_MSG_MISSING_RESULT);
        }
        User user = authService.getCurrentUser();
        BookEvaluation bookEvaluation = new BookEvaluation();
        if (user == null) {
            if (bookRateContents != null && !bookRateContents.isEmpty()) {
                String[] contents = cookieService.getCookieValuesByContents(bookRateContents);
                for (String itemBookIdRateId : contents) {
                    String[] components = itemBookIdRateId.split("-");
                    if (Integer.valueOf(components[0]).equals(bookId)) {
                        bookEvaluation = bookEvaluationRepository
                                .findById(Integer.valueOf(components[1])).orElse(new BookEvaluation());
                    }
                }
            }
        } else {
            bookEvaluation = bookEvaluationRepository
                    .findByBookAndUser(bookOpt.get(), user).orElse(new BookEvaluation());
        }
        bookEvaluation.setBook(bookOpt.get());
        bookEvaluation.setValue(value);
        bookEvaluation.setUser(user);
        bookEvaluation = bookEvaluationRepository.save(bookEvaluation);
        if (user == null) {
            String contentsValue = bookId + "-" + bookEvaluation.getId();
            cookieService.addValueToCookieResponse(response, bookRateContents,
                    BookShopConfig.BOOK_RATE_COOKIE_NAME, contentsValue);
        }
        return bookEvaluation;
    }

    public void addRecentlyViewLink(User user, Book book) {
        BookStatus bookStatus = getBookStatus(book, user);
        if (bookStatus != null && (bookStatus.equals(BookStatus.PAID) || bookStatus.equals(BookStatus.ARCHIVED))) {
            return;
        }
        Book2UserRecentlyView book2UserRecentlyView = new Book2UserRecentlyView();
        book2UserRecentlyView.setBook(book);
        book2UserRecentlyView.setUser(user);
        book2UserRecentlyView.setTime(LocalDateTime.now());
        book2UserRecentlyView.setBookUserId(new BookUserId(book.getId(), user.getId()));
        book2UserRecentlyViewRepository.save(book2UserRecentlyView);
    }

    /**
     * Метод возвращает статус привязанной книги к пользователю
     * @return BookStatus если связь существует, в противном случае null
     */
    public BookStatus getBookStatus(Book book, User user){
        Optional<Book2User> book2User = book2UserRepository.getBook2UserByUserAndBook(user, book);
        return book2User.map(value -> BookStatus.valueOf(value.getBook2UserType().getCode())).orElse(null);
    }

    public Set<Book> getViewedBooksByUser(User user){
        return book2UserRecentlyViewRepository
                .getAllByUserAndTimeAfterOrderByTimeDesc(user, LocalDateTime.now().minusDays(DAYS_EVALUATION_VIEWS));
    }
}