package com.example.bookshopapp.controllers;

import com.example.bookshopapp.api.dto.BookDto;
import com.example.bookshopapp.api.dto.GenreListDto;
import com.example.bookshopapp.api.request.StringRequest;
import com.example.bookshopapp.api.response.BookListResponse;
import com.example.bookshopapp.config.BookShopConfig;
import com.example.bookshopapp.config.LanguageMessage;
import com.example.bookshopapp.exception.BookListWrongParameterException;
import com.example.bookshopapp.exception.ViewEmptyParameterException;
import com.example.bookshopapp.exception.ViewNotFoundParameterException;
import com.example.bookshopapp.model.Genre;
import com.example.bookshopapp.model.Tag;
import com.example.bookshopapp.model.User;
import com.example.bookshopapp.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static com.example.bookshopapp.config.LanguageMessage.EX_MSG_RESOURCE_NOT_FOUND;

@Controller
@Slf4j
public class BooksController {
    private final BookService bookService;
    private final TagService tagService;
    private final GenreService genreService;
    private final AuthorService authorService;
    private final BooksRatingAndPopularityService booksRatingAndPopularityService;
    private final ReviewService reviewService;
    private final ResourceStorageService storage;
    private final AuthService authService;
    private final BookShopConfig bsConfig;

    @Autowired
    public BooksController(BookService bookService, TagService tagService,
                           GenreService genreService, AuthorService authorService,
                           BooksRatingAndPopularityService booksRatingAndPopularityService,
                           ResourceStorageService storage, ReviewService reviewService,
                           AuthService authService, BookShopConfig bsConfig) {
        this.bookService = bookService;
        this.tagService = tagService;
        this.genreService = genreService;
        this.authorService = authorService;
        this.booksRatingAndPopularityService = booksRatingAndPopularityService;
        this.reviewService = reviewService;
        this.storage = storage;
        this.authService = authService;
        this.bsConfig = bsConfig;
    }

    @ModelAttribute("recentBookList")
    public BookListResponse recentBookList() {
        return bookService.getPageOfRecentBooks(0, bsConfig.getThPageBookShowLimit(), "", "");
    }

    @ModelAttribute("popularBookList")
    public BookListResponse popularBookList() {
        return bookService.getPageOfPopularBooks(0, bsConfig.getThPageBookShowLimit());
    }

    @ModelAttribute("viewedBookList")
    public BookListResponse viewedBookList() {
        return bookService.getPageOfViewedBooks(0, bsConfig.getThPageBookShowLimit());
    }

    @ModelAttribute("genreList")
    public List<GenreListDto> genreList() {
        return genreService.getGenreStructure();
    }

    @GetMapping("/books/popular")
    public String popularBookPage() {
        return "books/popular";
    }

    @GetMapping("/books/recent")
    public String recentBookPage() {
        return "books/recent";
    }


    @GetMapping("/books/viewed")
    public String viewedBookPage() {
        return "books/viewed";
    }

    @GetMapping("/genres")
    public String genresBookPage() {
        return "genres/index";
    }

    @GetMapping("/genres/{slug}")
    public String genreBookPageBySlug(@PathVariable(value = "slug", required = false) StringRequest slugRequest,
                                      Model model) throws ViewNotFoundParameterException {
        if (slugRequest != null) {
            Genre genre = genreService.getGenreBySlug(slugRequest.getStr());
            BookListResponse bookListResponse = bookService.getPageOfBooksByGenreSlug(0,
                    bsConfig.getThPageBookShowLimit(), slugRequest.getStr());
            model.addAttribute("genre", genre);
            model.addAttribute("bookListResult", bookListResponse);
            return "/genres/slug";
        }
        throw new ViewNotFoundParameterException(LanguageMessage.EX_MSG_EMPTY_PARAM);
    }

    @GetMapping(value = {"/tags", "/tags/{slug}"})
    public String tagBookPageBySlug(@PathVariable(value = "slug", required = false) StringRequest slugRequest,
                                    Model model) throws ViewNotFoundParameterException {
        if (slugRequest != null) {
            Tag tag = tagService.getTagBySlug(slugRequest.getStr());
            BookListResponse bookListResponse = bookService.getPageOfBooksByTagSlug(0,
                    bsConfig.getThPageBookShowLimit(), slugRequest.getStr());
            model.addAttribute("tag", tag);
            model.addAttribute("bookListResult", bookListResponse);
            return "/tags/index";
        }
        throw new ViewNotFoundParameterException(LanguageMessage.EX_MSG_EMPTY_PARAM);
    }

    @GetMapping(value = {"/books", "/books/{slug}"})
    public String bookBySlug(@PathVariable(value = "slug", required = false) StringRequest slugRequest, Model model)
            throws IOException, ViewNotFoundParameterException {
        if (slugRequest != null) {
            BookDto bookDto = bookService.getBookDtoBySlugAndAddRecentlyView(slugRequest.getStr());
            model.addAttribute("slugBook", bookDto);
            model.addAttribute("distributionRating", booksRatingAndPopularityService
                    .getDistributionRatingBook(bookDto.getId()));
            model.addAttribute("tags", tagService.getTagsByBook(bookDto.getId()));
            model.addAttribute("authors", authorService.getAuthorsByBookId(bookDto.getId()));
            model.addAttribute("bookFiles", bookService.getBookFiles(bookDto.getId()));
            model.addAttribute("reviews", reviewService.getReviewByBookId(bookDto.getId()));
            User user = authService.getCurrentUser();
            if (user != null) {
                return "/books/slugmy";
            }
            return "/books/slug";
        }
        throw new ViewNotFoundParameterException(LanguageMessage.EX_MSG_EMPTY_PARAM);
    }

    @PostMapping("/books/{slug}/img/save")
    public String saveNewBookImage(@RequestParam("file") MultipartFile file,
                                   @PathVariable("slug") StringRequest slugRequest)
            throws IOException, BookListWrongParameterException, ViewEmptyParameterException {
        if (slugRequest != null) {
            String savePath = storage.saveNewBookImage(file, slugRequest.getStr());
            bookService.updateFileImage(savePath, slugRequest.getStr());
            return ("redirect:/books/" + slugRequest.getStr());
        }
        throw new ViewEmptyParameterException(LanguageMessage.EX_MSG_EMPTY_PARAM);
    }

    @GetMapping("/books/download/{hash}")
    public ResponseEntity<ByteArrayResource> bookFile(@PathVariable("hash") StringRequest hash)
            throws IOException, ViewNotFoundParameterException {
        if (hash != null) {
            if (!storage.isAvailableFileAndUpdateCountDownload(hash.getStr())) {
                throw new ViewNotFoundParameterException(EX_MSG_RESOURCE_NOT_FOUND);
            }
            byte[] data = storage.getFileByHash(hash.getStr());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" +
                            storage.getFilePathByHash(hash.getStr()).getFileName().toString())
                    .contentType(storage.getFileMediaTypeByHash(hash.getStr()))
                    .contentLength(data.length)
                    .body(new ByteArrayResource(data));
        }
        throw new ViewNotFoundParameterException(EX_MSG_RESOURCE_NOT_FOUND);
    }
}
