package com.example.bookshopapp.repositories;

import com.example.bookshopapp.model.Book;
import com.example.bookshopapp.model.Book2Author;
import com.example.bookshopapp.model.compositekey.BookAuthorId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Book2AuthorRepository extends JpaRepository<Book2Author, BookAuthorId> {
    List<Book2Author> getAllByBook(Book book);
}
