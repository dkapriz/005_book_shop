package com.example.bookshopapp.repositories;

import com.example.bookshopapp.model.BookReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookReviewRepository extends JpaRepository<BookReview, Integer> {

    @Query(value = "SELECT br FROM BookReview br " +
            "LEFT JOIN br.book b WHERE b.id = :bookId ORDER BY br.rating DESC")
    List<BookReview> findBookReviewsByBookId(@Param("bookId") Integer bookId);
}
