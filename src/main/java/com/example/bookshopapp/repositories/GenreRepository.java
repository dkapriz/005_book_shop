package com.example.bookshopapp.repositories;

import com.example.bookshopapp.model.Book;
import com.example.bookshopapp.model.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Integer> {
    Optional<Genre> getGenreBySlug(String slug);

    @Query(value = "SELECT g FROM Genre g " +
            "LEFT JOIN g.books b WHERE b IN :books")
    Set<Genre> getGenresByBooks(@Param("books") Set<Book> books);
}