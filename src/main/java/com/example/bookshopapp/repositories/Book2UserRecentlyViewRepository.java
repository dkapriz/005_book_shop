package com.example.bookshopapp.repositories;

import com.example.bookshopapp.model.Book;
import com.example.bookshopapp.model.Book2UserRecentlyView;
import com.example.bookshopapp.model.User;
import com.example.bookshopapp.model.compositekey.BookUserId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Set;

@Repository
public interface Book2UserRecentlyViewRepository extends JpaRepository<Book2UserRecentlyView, BookUserId> {

    Integer countAllByBookAndTimeAfter(Book book, LocalDateTime time);

    @Query(value = "SELECT b FROM Book b " +
            "LEFT JOIN b.recentlyViews brv " +
            "LEFT JOIN brv.user u " +
            "WHERE u = :user AND (brv.time BETWEEN :time AND CURRENT_TIMESTAMP) " +
            "ORDER BY brv.time DESC")
    Set<Book> getAllByUserAndTimeAfterOrderByTimeDesc(@Param("user") User user, @Param("time") LocalDateTime time);
}
