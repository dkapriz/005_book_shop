package com.example.bookshopapp.service;

import com.example.bookshopapp.api.dto.BookDto;
import com.example.bookshopapp.api.response.BookListResponse;
import com.example.bookshopapp.api.response.ResultResponse;
import com.example.bookshopapp.aspect.LoggingMethod;
import com.example.bookshopapp.config.BookShopConfig;
import com.example.bookshopapp.config.LanguageMessage;
import com.example.bookshopapp.exception.BookListWrongParameterException;
import com.example.bookshopapp.exception.ViewNotFoundParameterException;
import com.example.bookshopapp.exception.WrongParameterException;
import com.example.bookshopapp.model.*;
import com.example.bookshopapp.model.enums.BookStatus;
import com.example.bookshopapp.repositories.Book2AuthorRepository;
import com.example.bookshopapp.repositories.Book2UserRepository;
import com.example.bookshopapp.repositories.BookRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BookService {
    private final BookRepository bookRepository;
    private final Book2UserRepository book2UserRepository;
    private final Book2AuthorRepository book2AuthorRepository;
    private final ResourceStorageService resourceStorageService;
    private final BooksRatingAndPopularityService booksRatingAndPopularityService;
    private final TagService tagService;
    private final GenreService genreService;
    private final AuthService authService;

    @Autowired
    public BookService(BookRepository bookRepository, ResourceStorageService resourceStorageService,
                       Book2UserRepository book2UserRepository, Book2AuthorRepository book2AuthorRepository,
                       BooksRatingAndPopularityService booksRatingAndPopularityService, TagService tagService,
                       GenreService genreService, AuthService authService) {
        this.bookRepository = bookRepository;
        this.resourceStorageService = resourceStorageService;
        this.book2UserRepository = book2UserRepository;
        this.book2AuthorRepository = book2AuthorRepository;
        this.booksRatingAndPopularityService = booksRatingAndPopularityService;
        this.tagService = tagService;
        this.genreService = genreService;
        this.authService = authService;
    }

    /**
     * Выводится список рекомендуемых книг
     * Если пользователь не авторизирован, выводятся книги с сортировкой сначала по рейтингу, потом по новизне
     * Если пользователь авторизирован, то формируем список книг из его предпочтений.
     * Сначала анализируются просмотренные им книги за количество дней, определенных в константе DAYS_EVALUATION_VIEWS.
     * Затем происходит анализ в следующей последовательности: купленные -> добавленные в корзину -> отложенные.
     * После анализа происходит вывод книг по авторам, тегам и жанрам из группы книг пользователя и отсортированный
     * по новизне.
     * Если список книг пользователя пустой, то выводит по алгоритму не авторизированного пользователя
     */
    @LoggingMethod
    public BookListResponse getPageOfRecommendedBooks(Integer offset, Integer limit) {
        User user = authService.getCurrentUser();
        BookListResponse response = getRecommendedBooksByUserViews(offset, limit, user);
        if (response != null) {
            return response;
        }
        response = getRecommendedBooksByUserCard(offset, limit, user);
        if (response != null) {
            return response;
        }
        Pageable pageable = PageRequest.of(offset, limit,
                Sort.by(BookShopConfig.SORT_PARAM_RECOMMENDED, BookShopConfig.SORT_PARAM_POPULAR_INDEX).descending());
        Page<Book> result = bookRepository.findAll(pageable);
        return new BookListResponse(getBooksDto(result.getContent()), result.getTotalElements());
    }

    protected BookListResponse getRecommendedBooksByUserCard(Integer offset, Integer limit, User user) {
        if (user != null) {
            Set<Book> books = getSetBooksByStatus(BookStatus.PAID, user);
            if (books.isEmpty()) {
                books = getSetBooksByStatus(BookStatus.CART, user);
            }
            if (books.isEmpty()) {
                books = getSetBooksByStatus(BookStatus.KEPT, user);
            }
            if (!books.isEmpty()) {
                return getBooksByTagsOrAuthorsOrGenres(offset, limit, user, books);
            }
        }
        return null;
    }

    protected BookListResponse getRecommendedBooksByUserViews(Integer offset, Integer limit, User user) {
        if (user != null) {
            Set<Book> books = booksRatingAndPopularityService.getViewedBooksByUser(user);
            if (!books.isEmpty()) {
                return getBooksByTagsOrAuthorsOrGenres(offset, limit, user, books);
            }
        }
        return null;
    }

    private BookListResponse getBooksByTagsOrAuthorsOrGenres(Integer offset, Integer limit, User user, Set<Book> books) {
        Pageable pageable = PageRequest.of(offset, limit,
                Sort.by(BookShopConfig.SORT_PARAM_POPULAR_INDEX).descending());
        Page<Book> result = bookRepository.findBooksByTagsOrAuthorsOrGenres(tagService.getTagsByBooks(books),
                getBook2AuthorListByBooks(books), genreService.getGenresByBooks(books), user, pageable);
        return new BookListResponse(getBooksDto(result.getContent()), result.getTotalElements());
    }

    public BookListResponse getPageOfViewedBooks(Integer offset, Integer limit) {
        User user = authService.getCurrentUser();
        if (user == null) {
            return new BookListResponse(new ArrayList<>(), 0L);
        }
        Pageable pageable = PageRequest.of(offset, limit);
        Page<Book> result = bookRepository.findAllViewedBooksByUser(user, pageable);
        return new BookListResponse(getBooksDto(result.getContent()), result.getTotalElements());
    }

    public BookListResponse getPageOfRecentBooks(Integer offset, Integer limit, String fromDate, String toDate) {
        Pageable pageable = PageRequest.of(offset, limit,
                Sort.by(BookShopConfig.SORT_PARAM_PUBLICATION_DATE).descending());
        Page<Book> result;
        if (fromDate.isEmpty() && toDate.isEmpty()) {
            result = bookRepository.findAll(pageable);
            return new BookListResponse(getBooksDto(result.getContent()), result.getTotalElements());
        }
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(BookShopConfig.DATE_FORMAT_API);
        LocalDate localDateFrom;
        LocalDate localDateTo;

        if (fromDate.isEmpty()) {
            localDateTo = LocalDate.parse(toDate, dateTimeFormatter);
            result = bookRepository.findAllByPubDateBefore(localDateTo, pageable);
            return new BookListResponse(getBooksDto(result.getContent()), result.getTotalElements());
        }
        if (toDate.isEmpty()) {
            localDateFrom = LocalDate.parse(fromDate, dateTimeFormatter);
            result = bookRepository.findAllByPubDateAfter(localDateFrom, pageable);
            return new BookListResponse(getBooksDto(result.getContent()), result.getTotalElements());
        }
        localDateFrom = LocalDate.parse(fromDate, dateTimeFormatter);
        localDateTo = LocalDate.parse(toDate, dateTimeFormatter);
        result = bookRepository.findAllByPubDateBetween(localDateFrom, localDateTo, pageable);
        return new BookListResponse(getBooksDto(result.getContent()), result.getTotalElements());
    }

    public BookListResponse getPageOfPopularBooks(Integer offset, Integer limit) {
        Pageable pageable = PageRequest.of(offset, limit,
                Sort.by(BookShopConfig.SORT_PARAM_POPULAR_INDEX).descending());
        Page<Book> result = bookRepository.findAll(pageable);
        return new BookListResponse(getBooksDto(result.getContent()), result.getTotalElements());
    }

    public BookListResponse getPageOfSearchResultBook(Integer offset, Integer limit, String searchWord) {
        Pageable pageable = PageRequest.of(offset, limit);
        Page<Book> result = bookRepository.findAllByTitleContainingIgnoreCase(searchWord, pageable);
        return new BookListResponse(getBooksDto(result.getContent()), result.getTotalElements());
    }

    public BookListResponse getPageOfBooksByTagId(Integer offset, Integer limit, Integer id) {
        Pageable pageable = PageRequest.of(offset, limit);
        Page<Book> result = bookRepository.findAllByTagId(pageable, id);
        return new BookListResponse(getBooksDto(result.getContent()), result.getTotalElements());
    }

    public BookListResponse getPageOfBooksByTagSlug(Integer offset, Integer limit, String slug) {
        Pageable pageable = PageRequest.of(offset, limit);
        Page<Book> result = bookRepository.findAllByTagSlugName(pageable, slug);
        return new BookListResponse(getBooksDto(result.getContent()), result.getTotalElements());
    }

    public BookListResponse getPageOfBooksByGenreId(Integer offset, Integer limit, Integer id) {
        Pageable pageable = PageRequest.of(offset, limit,
                Sort.by(BookShopConfig.SORT_PARAM_PUBLICATION_DATE).descending());
        Page<Book> result = bookRepository.findAllByGenreId(pageable, id);
        return new BookListResponse(getBooksDto(result.getContent()), result.getTotalElements());
    }

    public BookListResponse getPageOfBooksByGenreSlug(Integer offset, Integer limit, String slug) {
        Pageable pageable = PageRequest.of(offset, limit,
                Sort.by(BookShopConfig.SORT_PARAM_PUBLICATION_DATE).descending());
        Page<Book> result = bookRepository.findAllByGenreSlugName(pageable, slug);
        return new BookListResponse(getBooksDto(result.getContent()), result.getTotalElements());
    }

    public BookListResponse getPageOfBooksByAuthorId(Integer offset, Integer limit, Integer id) {
        Pageable pageable = PageRequest.of(offset, limit,
                Sort.by(BookShopConfig.SORT_PARAM_PUBLICATION_DATE).descending());
        Page<Book> result = bookRepository.findAllByAuthorId(pageable, id);
        return new BookListResponse(getBooksDto(result.getContent()), result.getTotalElements());
    }

    public BookListResponse getPageOfBooksByAuthorSlug(Integer offset, Integer limit, String slug) {
        Pageable pageable = PageRequest.of(offset, limit,
                Sort.by(BookShopConfig.SORT_PARAM_PUBLICATION_DATE).descending());
        Page<Book> result = bookRepository.findAllByAuthorSlugName(pageable, slug);
        return new BookListResponse(getBooksDto(result.getContent()), result.getTotalElements());
    }

    public List<BookDto> getBookListDtoUserByStatus(BookStatus status) {
        return getBooksDto(getBookListAuthUserByStatus(status));
    }

    public List<BookDto> getBooksDtoBySlugs(String[] slugs) {
        return bookRepository.findBooksBySlugIn(Arrays.asList(slugs))
                .stream().map(this::getBookDto).collect(Collectors.toList());
    }

    public BookDto getBookDtoBySlugAndAddRecentlyView(String slug) throws ViewNotFoundParameterException {
        try {
            Book book = getBookBySlug(slug);
            User user = authService.getCurrentUser();
            if (user != null) {
                booksRatingAndPopularityService.addRecentlyViewLink(user, book);
            }
            return getBookDto(book);
        } catch (WrongParameterException e) {
            throw new ViewNotFoundParameterException(e.getMessage());
        }
    }

    public List<Book> getBookListAuthUserByStatus(BookStatus status) {
        User user = authService.getCurrentUser();
        if (user == null) {
            return new ArrayList<>();
        }
        return book2UserRepository.getBooksByStatusAndUserId(user.getId(), status.getStatus());
    }

    public Book getBookBySlug(String slug) throws WrongParameterException {
        Optional<Book> result = bookRepository.findBookBySlug(slug);
        if (!result.isPresent()) {
            log.warn("getBookBySlug (handling null value) slug:" + slug);
            throw new WrongParameterException(LanguageMessage.EX_MSG_MISSING_RESULT);
        }
        return result.get();
    }

    public Book getBookById(Integer bookId) throws WrongParameterException {
        Optional<Book> bookOptional = bookRepository.findById(bookId);
        if (!bookOptional.isPresent()) {
            log.warn("getBookById (handling null value) id:" + bookId);
            throw new WrongParameterException(LanguageMessage.EX_MSG_MISSING_RESULT);
        }
        return bookOptional.get();
    }

    public List<Book> getBooksBySlugs(String[] slugs) {
        return bookRepository.findBooksBySlugIn(Arrays.asList(slugs));
    }

    public Set<Book> getSetBooksByStatus(BookStatus status, User user) {
        return book2UserRepository.getSetBooksByStatusAndUserId(user.getId(), status.getStatus());
    }

    public List<Book> getListBooksByStatus(BookStatus status, User user) {
        return book2UserRepository.getListBooksByStatusAndUserId(user.getId(), status.getStatus());
    }

    public String getSlugBook(Integer bookId) throws WrongParameterException {
        Optional<Book> book = bookRepository.findById(bookId);
        if (!book.isPresent()) {
            log.warn("getSlugBook (handling null value) bookId:" + bookId);
            throw new WrongParameterException(LanguageMessage.EX_MSG_MISSING_RESULT);
        }
        return book.get().getSlug();
    }

    public void updateFileImage(String path, String slug) throws BookListWrongParameterException {
        Optional<Book> bookOpt = bookRepository.findBookBySlug(slug);
        if (!bookOpt.isPresent()) {
            log.warn("updateFileImage (handling null value) slug:" + slug);
            throw new BookListWrongParameterException(LanguageMessage.EX_MSG_MISSING_RESULT);
        }
        bookOpt.get().setImage(path);
        bookRepository.save(bookOpt.get());
    }

    /**
     * Метод отправляет список файлов пользователя и проверяет, что книга для пользователя находится в статусе PAID
     * или ARCHIVED
     *
     * @param bookId - id книги
     * @return ResultResponse(false) - если превышено количество скачиваний файлов
     * BookFileListDto(true, EmptySet) - если файлы не найдены
     * BookFileListDto(true, Set) - в случае успешного выполнения
     * @throws IOException             - ошибка чтения файла
     * @throws WrongParameterException - ошибка входных параметров
     */
    public ResultResponse getBookFiles(Integer bookId) throws IOException, WrongParameterException {
        User user = authService.getCurrentUser();
        if (user == null) {
            return new ResultResponse(false);
        }
        Optional<Book> bookOpt = bookRepository.findById(bookId);
        if (!bookOpt.isPresent()) {
            throw new WrongParameterException(LanguageMessage.EX_MSG_MISSING_RESULT);
        }
        if (!resourceStorageService.isAvailableFile(bookOpt.get(), user, false)) {
            return new ResultResponse(false);
        }
        return resourceStorageService.getBooksFiles(bookOpt.get());
    }

    public List<BookDto> getBooksDto(List<Book> books) {
        if (books.isEmpty()) {
            return new ArrayList<>();
        }
        return books.stream().map(this::getBookDto).collect(Collectors.toList());
    }

    public BookDto getBookDto(Book book) {
        List<Author> authorsSortedList = getAuthorsNameBySortIndex(book);
        return new BookDto(
                book.getId(),
                book.getSlug(),
                book.getImage(),
                getAuthorName(authorsSortedList),
                getAuthorSlugName(authorsSortedList),
                book.getTitle(),
                (int) book.getDiscount(),
                book.getIsBestseller(),
                Math.toIntExact(Math.round(book.getRating())),
                getStatusBook(book),
                book.getPrice(),
                getDiscountPrice(book.getPrice(), book.getDiscount()),
                book.getDescription()
        );
    }

    private List<Book2Author> getBook2AuthorListByBooks(Set<Book> books) {
        if (books.isEmpty()) {
            return new ArrayList<>();
        }
        List<Book2Author> book2Authors = new ArrayList<>();
        for (Book book : books) {
            book2Authors.addAll(book2AuthorRepository.getAllByBook(book));
        }
        return book2Authors;
    }

    public String getStatusBook(Book book) {
        User user = authService.getCurrentUser();
        if (user == null) {
            return "false";
        }
        Optional<Book2User> book2Users = book2UserRepository
                .getBook2UserByBookAndUserId(book.getId(), user.getId());
        if (!book2Users.isPresent()) {
            return "false";
        }
        return book2Users.get().getBook2UserType().getCode();
    }

    public static Integer getDiscountPrice(Integer price, Byte discount) {
        return price - Math.round(price * discount / 100f);
    }

    private String getAuthorName(List<Author> authors) {
        if (authors.isEmpty()) {
            return "";
        }
        String authorName = authors.get(0).getName();
        if (authors.size() > 1) {
            authorName = LanguageMessage.getAuthorsName(authorName);
        }
        return authorName;
    }

    private String getAuthorSlugName(List<Author> authors) {
        if (authors.isEmpty()) {
            return "";
        }
        return authors.get(0).getSlug();
    }

    private List<Author> getAuthorsNameBySortIndex(Book book) {
        if (book.getAuthors() == null) {
            return new ArrayList<>();
        }
        return book.getAuthors().stream()
                .sorted(Comparator.comparing(Book2Author::getSortIndex))
                .map(Book2Author::getAuthor)
                .collect(Collectors.toList());
    }

    public Integer getTotalPrice(List<BookDto> bookDtoList) {
        if (bookDtoList.isEmpty()) {
            return 0;
        }
        return bookDtoList.stream()
                .map(BookDto::getPrice)
                .reduce(Integer::sum)
                .orElse(0);
    }

    public Integer getTotalDiscountPrice(List<BookDto> bookDtoList) {
        if (bookDtoList.isEmpty()) {
            return 0;
        }
        return bookDtoList.stream()
                .map(BookDto::getDiscountPrice)
                .reduce(Integer::sum).orElse(0);
    }
}