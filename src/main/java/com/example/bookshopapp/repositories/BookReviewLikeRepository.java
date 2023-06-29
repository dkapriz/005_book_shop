package com.example.bookshopapp.repositories;

import com.example.bookshopapp.model.BookReview;
import com.example.bookshopapp.model.BookReviewLike;
import com.example.bookshopapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookReviewLikeRepository extends JpaRepository<BookReviewLike, Integer> {
    Optional<BookReviewLike> findBookReviewLikeByBookReviewAndUser(BookReview bookReview, User user);
    Optional<BookReviewLike> findBookReviewLikeByBookReviewAndHashCodeIn(BookReview bookReview, List<String> hashCode);
}
