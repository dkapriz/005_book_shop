package com.example.bookshopapp.service;

import com.example.bookshopapp.api.dto.GenreListDto;
import com.example.bookshopapp.config.LanguageMessage;
import com.example.bookshopapp.exception.BookListWrongParameterException;
import com.example.bookshopapp.exception.ViewNotFoundParameterException;
import com.example.bookshopapp.model.Book;
import com.example.bookshopapp.model.Genre;
import com.example.bookshopapp.repositories.GenreRepository;
import com.example.bookshopapp.service.utils.GenreNodeHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ForkJoinPool;

@Service
@Slf4j
public class GenreService {

    private final GenreRepository genreRepository;

    @Autowired
    public GenreService(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    public List<Genre> getGenres() {
        return genreRepository.findAll();
    }

    public Set<Genre> getGenresByBooks(Set<Book> books) {
        if (books.isEmpty()) {
            return new HashSet<>();
        }
        return genreRepository.getGenresByBooks(books);
    }

    public Genre getGenreBySlug(String slug) throws ViewNotFoundParameterException {
        Optional<Genre> genre = genreRepository.getGenreBySlug(slug);
        if (!genre.isPresent()) {
            log.warn("getGenreBySlug (handling null value) slug:" + slug);
            throw new ViewNotFoundParameterException(LanguageMessage.EX_MSG_MISSING_RESULT);
        }
        return genre.get();
    }

    public List<GenreListDto> getGenreStructure() {
        List<Genre> genres = genreRepository.findAll();
        genres.forEach(genre -> {
            if (genre.getParentId() == null) {
                genre.setParentId(0);
            }
        });
        genres.sort(Comparator.comparing(Genre::getParentId));
        return new ForkJoinPool().invoke(new GenreNodeHandler(genres)).getGenreList();
    }
}
