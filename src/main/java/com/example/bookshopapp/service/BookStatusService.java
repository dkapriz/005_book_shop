package com.example.bookshopapp.service;

import com.example.bookshopapp.api.dto.BookDto;
import com.example.bookshopapp.config.BookShopConfig;
import com.example.bookshopapp.config.LanguageMessage;
import com.example.bookshopapp.exception.WrongParameterException;
import com.example.bookshopapp.model.Book;
import com.example.bookshopapp.model.Book2User;
import com.example.bookshopapp.model.Book2UserType;
import com.example.bookshopapp.model.User;
import com.example.bookshopapp.model.compositekey.BookUserId;
import com.example.bookshopapp.model.enums.BookStatus;
import com.example.bookshopapp.repositories.Book2UserRepository;
import com.example.bookshopapp.repositories.Book2UserTypeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class BookStatusService {
    private final BookService bookService;
    private final CookieService cookieService;
    private final AuthService authService;
    private final Book2UserRepository book2UserRepository;
    private final Book2UserTypeRepository book2UserTypeRepository;

    @Autowired
    public BookStatusService(BookService bookService, CookieService cookieService, AuthService authService,
                             Book2UserRepository book2UserRepository, Book2UserTypeRepository book2UserTypeRepository) {
        this.bookService = bookService;
        this.cookieService = cookieService;
        this.authService = authService;
        this.book2UserRepository = book2UserRepository;
        this.book2UserTypeRepository = book2UserTypeRepository;
    }

    /**
     * Обработчик изменения статуса книги.
     * Проверяется, вошел пользователь или нет
     * Если пользователь вошел, то работа со статусами из базы данных
     * В противном случае обрабатываются cookie
     */
    public void handleChangeBookStatus(List<Integer> booksIds, String status,
                                       String cartContents, String postponedContents,
                                       HttpServletResponse response)
            throws WrongParameterException {
        List<String> bookSlugs = new ArrayList<>();
        for (Integer bookId : booksIds) {
            bookSlugs.add(bookService.getSlugBook(bookId));
        }
        User user = authService.getCurrentUser();
        if (user == null) {
            changeCookieBookStatus(bookSlugs, status, cartContents, postponedContents, response);
        } else {
            changeDataBaseBookStatus(user, bookSlugs, status);
        }
    }

    private void changeDataBaseBookStatus(User user, List<String> bookSlugs, String status)
            throws WrongParameterException {
        for (String slug : bookSlugs) {
            Book book = bookService.getBookBySlug(slug);
            if (BookStatus.valueOf(status).equals(BookStatus.KEPT) ||
                    BookStatus.valueOf(status).equals(BookStatus.CART) ||
                    BookStatus.valueOf(status).equals(BookStatus.UNLINK)) {
                changeOrCreateBook2UserLink(user, book.getId(), BookStatus.valueOf(status));
            }
            if (BookStatus.valueOf(status).equals(BookStatus.PAID) ||
                    BookStatus.valueOf(status).equals(BookStatus.ARCHIVED)) {
                changePaidBook2UserLink(user, book.getId(), BookStatus.valueOf(status));
            }
        }
    }

    private void changeCookieBookStatus(List<String> bookSlugs, String status,
                                        String cartContents, String postponedContents,
                                        HttpServletResponse response) throws WrongParameterException {
        switch (BookStatus.valueOf(status)) {
            case KEPT:
                cookieService.addValueToCookieResponse(response, postponedContents,
                        BookShopConfig.POSTPONED_COOKIE_NAME, bookSlugs);
                cookieService.deleteValueFromCookieBooksResponse(response, cartContents,
                        BookShopConfig.CART_COOKIE_NAME, bookSlugs);
                break;
            case CART:
                cookieService.addValueToCookieResponse(response, cartContents,
                        BookShopConfig.CART_COOKIE_NAME, bookSlugs);
                cookieService.deleteValueFromCookieBooksResponse(response, postponedContents,
                        BookShopConfig.POSTPONED_COOKIE_NAME, bookSlugs);
                break;
            case UNLINK:
                cookieService.deleteValueFromCookieBooksResponse(response, cartContents,
                        BookShopConfig.CART_COOKIE_NAME, bookSlugs);
                cookieService.deleteValueFromCookieBooksResponse(response, postponedContents,
                        BookShopConfig.POSTPONED_COOKIE_NAME, bookSlugs);
                break;
            default:
                throw new WrongParameterException(LanguageMessage.EX_MSG_WRONG_PARAM);
        }
    }

    public List<BookDto> getBooksFromCookie(String contents) {
        if (contents == null || contents.isEmpty()) {
            return new ArrayList<>();
        }
        return bookService.getBooksDtoBySlugs(cookieService.getCookieValuesByContents(contents));
    }

    /**
     * Изменение связи между пользователем и книгой
     * Если связь найдена в базе данных и тип связи не равен PAID или ARCHIVED, то обновляем данные связи
     * Если статус равен UNLINK, то удаляем связь
     * Если связь не найдена, создаем новую
     */
    @Transactional
    public void changeOrCreateBook2UserLink(User user, Integer bookId, BookStatus status)
            throws WrongParameterException {
        Optional<Book2User> book2User = book2UserRepository.getBook2UserByBookAndUserId(bookId, user.getId());
        if (book2User.isPresent()) {
            if (!book2User.get().getBook2UserType().getCode().equals(BookStatus.PAID.getStatus()) &&
                    !book2User.get().getBook2UserType().getCode().equals(BookStatus.ARCHIVED.getStatus())) {
                if (status.equals(BookStatus.UNLINK)) {
                    deleteBook2UserLink(book2User.get());
                    return;
                }
                changeBook2UserStatus(book2User.get(), status);
            }
        } else {
            createLinkBook2User(user, bookId, status);
        }
    }

    /**
     * Изменение связи между пользователем и книгой
     * если тип связи и статус равен PAID или ARCHIVED
     */
    @Transactional
    public void changePaidBook2UserLink(User user, Integer bookId, BookStatus status) {
        if (!status.equals(BookStatus.PAID) && !status.equals(BookStatus.ARCHIVED)) {
            return;
        }
        Optional<Book2User> book2User = book2UserRepository.getBook2UserByBookAndUserId(bookId, user.getId());
        if (book2User.isPresent() &&
                (book2User.get().getBook2UserType().getCode().equals(BookStatus.PAID.getStatus()) ||
                        book2User.get().getBook2UserType().getCode().equals(BookStatus.ARCHIVED.getStatus()))) {
            changeBook2UserStatus(book2User.get(), status);
        }
    }

    /**
     * Сохранение новой связи между книгой и пользователем в базу данных
     */
    @Transactional
    public void createLinkBook2User(User user, Integer bookId, BookStatus status)
            throws WrongParameterException {
        if (status.equals(BookStatus.UNLINK)) {
            return;
        }
        Book2User book2User = new Book2User();
        book2User.setUser(user);
        book2User.setBook(bookService.getBookById(bookId));
        book2User.setBook2UserType(getBook2UserTypeByStatus(status));
        book2User.setTime(LocalDateTime.now());
        book2User.setBookUserId(new BookUserId(user.getId(), bookId));
        book2UserRepository.save(book2User);
        log.info("BookService - create book to user link status: " + status + " with id: " + book2User.getBookUserId());
    }

    private Book2UserType getBook2UserTypeByStatus(BookStatus status){
        Book2UserType book2UserType = new Book2UserType();
        Optional<Book2UserType> book2UserTypeOptional =
                book2UserTypeRepository.getBook2UserTypeByCode(status.getStatus());
        if (book2UserTypeOptional.isPresent()) {
            book2UserType = book2UserTypeOptional.get();
        } else {
            book2UserType.setCode(status.getStatus());
            book2UserType.setName(status.getName());
            book2UserType = book2UserTypeRepository.save(book2UserType);
        }
        return  book2UserType;
    }

    public Integer getCountBooksFromDataBase(User user, BookStatus status) {
        return book2UserRepository.countBook2UserByUserAndStatusBook(user.getId(), status.getStatus());
    }

    public void deleteBook2UserLink(Book2User book2User) {
        book2UserRepository.delete(book2User);
        log.info("BookService - delete book to user link with id: " + book2User.getBookUserId());
    }

    private void changeBook2UserStatus(Book2User book2User, BookStatus status) {
        book2User.setBook2UserType(getBook2UserTypeByStatus(status));
        book2User.setTime(LocalDateTime.now());
        book2UserRepository.save(book2User);
        log.info("BookService - change book to user link status: " + status + " with id: " + book2User.getBookUserId());
    }

    public List<BookDto> getBooksDtoByStatus(BookStatus status, User user) {
        return bookService.getBooksDto(getBooksByStatus(status, user));
    }

    public List<Book> getBooksByStatus(BookStatus status, User user) {
        return book2UserRepository.getBooksByStatusAndUserId(user.getId(), status.getStatus());
    }

    public void movePostponedAndCartFromCookieToDB(HttpServletRequest request, HttpServletResponse response) {
        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals(BookShopConfig.CART_COOKIE_NAME)) {
                moveBooksFromCookieToDB(cookie, BookStatus.CART, response);
            }
            if (cookie.getName().equals(BookShopConfig.POSTPONED_COOKIE_NAME)) {
                moveBooksFromCookieToDB(cookie, BookStatus.KEPT, response);
            }
        }
    }

    private void moveBooksFromCookieToDB(Cookie cookie, BookStatus status, HttpServletResponse response) {
        String[] slugs = cookieService.getCookieValuesByContents(cookie.getValue());
        List<Book> bookList = bookService.getBooksBySlugs(slugs);
        User user = authService.getCurrentUser();
        for (Book book : bookList) {
            if (!book2UserRepository.getBook2UserByBookAndUserId(book.getId(), user.getId()).isPresent()) {
                try {
                    createLinkBook2User(user, book.getId(), status);
                } catch (WrongParameterException e) {
                    return;
                }
            }
        }
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}