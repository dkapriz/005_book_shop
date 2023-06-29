package com.example.bookshopapp.repositories;

import com.example.bookshopapp.model.Book;
import com.example.bookshopapp.model.BookEvaluation;
import com.example.bookshopapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookEvaluationRepository extends JpaRepository<BookEvaluation, Integer> {
    List<BookEvaluation> findAllByBookAndValueNot(Book book, byte value);

    List<BookEvaluation> findAllByBookAndValue(Book book, byte value);

    Optional<BookEvaluation> findByBookAndUser(Book book, User user);
}
