package com.example.bookshopapp.service;

import com.example.bookshopapp.api.dto.ReviewDto;
import com.example.bookshopapp.config.BookShopConfig;
import com.example.bookshopapp.config.LanguageMessage;
import com.example.bookshopapp.exception.WrongParameterException;
import com.example.bookshopapp.model.Book;
import com.example.bookshopapp.model.BookReview;
import com.example.bookshopapp.model.BookReviewLike;
import com.example.bookshopapp.model.User;
import com.example.bookshopapp.repositories.BookReviewLikeRepository;
import com.example.bookshopapp.repositories.BookReviewRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReviewService {
    private final BookReviewRepository bookReviewRepository;
    private final BookReviewLikeRepository bookReviewLikeRepository;
    private final CookieService cookieService;
    private final BookService bookService;
    private final AuthService authService;

    @Autowired
    public ReviewService(BookReviewRepository bookReviewRepository, BookReviewLikeRepository bookReviewLikeRepository,
                         CookieService cookieService, BookService bookService, AuthService authService) {
        this.bookReviewRepository = bookReviewRepository;
        this.bookReviewLikeRepository = bookReviewLikeRepository;
        this.cookieService = cookieService;
        this.bookService = bookService;
        this.authService = authService;
    }

    @Scheduled(cron = BookShopConfig.BOOK_UPDATE_FREQUENCY)
    public void updateReviewsRating() {
        bookReviewRepository.findAll().forEach(this::updateReviewRating);
        log.info("Update reviews rating");
    }

    protected BookReview updateReviewRating(BookReview review) {
        int rating = review.getBookReviewLikes().stream()
                .map(likes -> Integer.valueOf(likes.getValue())).reduce(Integer::sum).orElse(0);
        review.setRating(rating);
        return bookReviewRepository.save(review);
    }

    public List<ReviewDto> getReviewByBookId(Integer bookId) {
        List<BookReview> bookReviews = bookReviewRepository.findBookReviewsByBookId(bookId);
        if (bookReviews.isEmpty()) {
            return new ArrayList<>();
        }
        return bookReviews.stream().map(bookReview -> {
            Integer countLike = getCountReviewLikesByValue(bookReview, BookShopConfig.REVIEW_LIKE);
            Integer countDislike = getCountReviewLikesByValue(bookReview, BookShopConfig.REVIEW_DISLIKE);
            return getReviewDto(bookReview, countLike, countDislike);
        }).collect(Collectors.toList());
    }

    private Integer getCountReviewLikesByValue(BookReview bookReview, byte value) {
        if (bookReview.getBookReviewLikes() == null || bookReview.getBookReviewLikes().isEmpty()) {
            return 0;
        }
        return Math.toIntExact(bookReview.getBookReviewLikes().stream()
                .map(BookReviewLike::getValue)
                .filter(v -> v.equals(value))
                .count());
    }

    private ReviewDto getReviewDto(BookReview bookReview, Integer likeCount, Integer dislikeCount) {
        String shortText = getShortReview(bookReview.getText());
        String textExtension = bookReview.getText().substring(shortText.length());
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(BookShopConfig.DATE_TIME_FORMAT_VIEW);
        return new ReviewDto(
                bookReview.getId(),
                bookReview.getUser().getName(),
                bookReview.getTime().format(dateTimeFormatter),
                shortText,
                textExtension,
                bookReview.getRating(),
                likeCount,
                dislikeCount
        );
    }

    private String getShortReview(String review) {
        if (review.isEmpty()) {
            return "";
        }
        if (review.length() < BookShopConfig.REVIEW_MAX_SHORT_LENGTH) {
            return review;
        }
        Pattern pattern = Pattern.compile(".{" + BookShopConfig.REVIEW_MIN_SHORT_LENGTH + "," +
                BookShopConfig.REVIEW_MAX_SHORT_LENGTH + "}\\.\\s");
        Matcher matcher = pattern.matcher(review);
        if (matcher.find()) {
            return matcher.group();
        }
        return "";
    }

    public boolean addBookReviewRate(Integer reviewId, Byte value, String rateBookReviewHashContents,
                                     HttpServletResponse response) throws WrongParameterException {
        if (reviewId <= 0) {
            throw new WrongParameterException(LanguageMessage.EX_MSG_WRONG_PARAM);
        }
        if (value != BookShopConfig.REVIEW_LIKE && value != BookShopConfig.REVIEW_DISLIKE && value != 0) {
            throw new WrongParameterException(LanguageMessage.EX_MSG_WRONG_PARAM);
        }
        Optional<BookReview> bookReview = bookReviewRepository.findById(reviewId);
        if (!bookReview.isPresent()) {
            throw new WrongParameterException(LanguageMessage.EX_MSG_WRONG_PARAM);
        }
        User user = authService.getCurrentUser();
        if (user != null) {
            addBookReviewLikeByUser(bookReview.get(), user, value);
        } else {
            addBookReviewLikeByCookie(bookReview.get(), value, rateBookReviewHashContents, response);
        }
        return true;
    }

    public boolean addBookReview(Integer bookId, String text) throws WrongParameterException {
        User user = authService.getCurrentUser();
        if (user == null) {
            throw new UsernameNotFoundException(LanguageMessage.EX_MSG_USER_NOT_FOUND);
        }
        if (text == null || bookId == null || text.isEmpty()) {
            throw new WrongParameterException(LanguageMessage.EX_MSG_WRONG_PARAM);
        }
        Book book = bookService.getBookById(bookId);
        BookReview bookReview = new BookReview();
        bookReview.setBook(book);
        bookReview.setUser(user);
        bookReview.setText(text);
        bookReview.setTime(LocalDateTime.now());
        bookReview.setRating(0);
        bookReviewRepository.save(bookReview);
        return true;
    }

    private void addBookReviewLikeByUser(BookReview bookReview, User user, Byte value) {
        BookReviewLike bookReviewLike = bookReviewLikeRepository
                .findBookReviewLikeByBookReviewAndUser(bookReview, user)
                .orElse(new BookReviewLike(user, bookReview));
        bookReviewLike.setValue(value);
        bookReviewLikeRepository.save(bookReviewLike);
    }

    private void addBookReviewLikeByCookie(BookReview bookReview, Byte value, String rateBookReviewHashContents,
                                           HttpServletResponse response) {
        BookReviewLike bookReviewLike;
        if (rateBookReviewHashContents == null || rateBookReviewHashContents.isEmpty()) {
            String hashCode = String.valueOf(bookReview.hashCode());
            bookReviewLike = new BookReviewLike(hashCode, bookReview);
            cookieService.addValueToCookieResponse(response, rateBookReviewHashContents,
                    BookShopConfig.BOOK_REVIEW_HASH_COOKIE_NAME, hashCode);
        } else {
            Optional<BookReviewLike> bookReviewLikeOptional = bookReviewLikeRepository
                    .findBookReviewLikeByBookReviewAndHashCodeIn(bookReview,
                            Arrays.asList(cookieService.getCookieValuesByContents(rateBookReviewHashContents)));
            if (bookReviewLikeOptional.isPresent()) {
                bookReviewLike = bookReviewLikeOptional.get();
            } else {
                String hashCode = String.valueOf(bookReview.hashCode());
                bookReviewLike = new BookReviewLike(hashCode, bookReview);
                cookieService.addValueToCookieResponse(response, rateBookReviewHashContents,
                        BookShopConfig.BOOK_REVIEW_HASH_COOKIE_NAME, hashCode);
            }
        }
        bookReviewLike.setValue(value);
        bookReviewLikeRepository.save(bookReviewLike);
    }
}
