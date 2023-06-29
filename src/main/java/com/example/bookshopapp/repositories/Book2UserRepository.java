package com.example.bookshopapp.repositories;

import com.example.bookshopapp.model.Book;
import com.example.bookshopapp.model.Book2User;
import com.example.bookshopapp.model.User;
import com.example.bookshopapp.model.compositekey.BookUserId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface Book2UserRepository extends JpaRepository<Book2User, BookUserId> {
    @Query(value = "SELECT COUNT(b2u) FROM Book2User b2u " +
            "LEFT JOIN b2u.book b " +
            "LEFT JOIN b2u.book2UserType b2ut " +
            "WHERE b.id = :bookId AND b2ut.code = :status")
    Integer countBooksByStatusAndBookId(@Param("status") String status, @Param("bookId") Integer bookId);

    @Query(value = "SELECT b2u FROM Book2User b2u " +
            "LEFT JOIN b2u.user u " +
            "LEFT JOIN b2u.book b " +
            "WHERE u.id = :userId AND b.id = :bookId ORDER BY b2u.time ASC")
    Optional<Book2User> getBook2UserByBookAndUserId(@Param("bookId") Integer bookId, @Param("userId") Integer userId);

    @Query(value = "SELECT b2u.book FROM Book2User b2u " +
            "LEFT JOIN b2u.user u " +
            "LEFT JOIN b2u.book2UserType b2ut " +
            "WHERE u.id = :userId AND b2ut.code = :status")
    List<Book> getBooksByStatusAndUserId(@Param("userId") Integer userId, @Param("status") String status);

    @Query(value = "SELECT b2u.book FROM Book2User b2u " +
            "LEFT JOIN b2u.user u " +
            "LEFT JOIN b2u.book2UserType b2ut " +
            "WHERE u.id = :userId AND b2ut.code = :status")
    Set<Book> getSetBooksByStatusAndUserId(@Param("userId") Integer userId, @Param("status") String status);

    @Query(value = "SELECT b2u.book FROM Book2User b2u " +
            "LEFT JOIN b2u.user u " +
            "LEFT JOIN b2u.book2UserType b2ut " +
            "WHERE u.id = :userId AND b2ut.code = :status")
    List<Book> getListBooksByStatusAndUserId(@Param("userId") Integer userId, @Param("status") String status);

    @Query(value = "SELECT COUNT(b2u) FROM Book2User b2u " +
            "LEFT JOIN b2u.user u " +
            "LEFT JOIN b2u.book2UserType b2ut " +
            "WHERE u.id = :userId AND b2ut.code = :status")
    Integer countBook2UserByUserAndStatusBook(@Param("userId") Integer userId, @Param("status") String status);

    Optional<Book2User> getBook2UserByUserAndBook(User user, Book book);
}
