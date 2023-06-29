package com.example.bookshopapp.repositories;

import com.example.bookshopapp.model.Book;
import com.example.bookshopapp.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface TagRepository extends JpaRepository<Tag, Integer> {
    Optional<Tag> getTagBySlug(String slug);

    @Query(value = "SELECT t FROM Tag t " +
            "LEFT JOIN t.books b WHERE b.id = :bookId")
    List<Tag> getTagsByBook(@Param("bookId") Integer bookId);

    @Query(value = "SELECT t FROM Tag t " +
            "LEFT JOIN t.books b WHERE b IN :books")
    Set<Tag> getTagsByBooks(@Param("books") Set<Book> books);
}
