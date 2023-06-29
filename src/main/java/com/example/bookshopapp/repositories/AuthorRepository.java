package com.example.bookshopapp.repositories;

import com.example.bookshopapp.model.Author;
import com.example.bookshopapp.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Integer> {
    Optional<Author> getAuthorBySlug(String slug);

    @Query(value = "SELECT a FROM Author a " +
            "LEFT JOIN a.books b WHERE b.book.id = :bookId ORDER BY b.sortIndex ASC")
    List<Author> getAuthorByBookId(@Param("bookId") Integer bookId);

    @Query(value = "SELECT a FROM Author a " +
            "LEFT JOIN a.books b WHERE b IN :books")
    List<Author> getAuthorsByBooks(@Param("books") Set<Book> books);
}
