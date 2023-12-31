package com.example.bookshopapp.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "book_review_like")
@ApiModel(description = "data model of book review like entity")
public class BookReviewLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty("id generated by db automatically")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id", columnDefinition = "INT")
    private User user;

    @Column(name = "hash_code", columnDefinition = "VARCHAR(255)")
    @ApiModelProperty("hash code for cookie")
    private String hashCode;

    @ManyToOne
    @JoinColumn(name = "review_id", columnDefinition = "INT", nullable = false)
    private BookReview bookReview;

    @Column(columnDefinition = "TIMESTAMP", nullable = false)
    @ApiModelProperty("the date and time at which the like or dislike is set")
    private LocalDateTime time;

    @Column(columnDefinition = "INT2", nullable = false)
    @ApiModelProperty("like (1) or dislike (-1)")
    private byte value;

    public BookReviewLike(User user, BookReview bookReview) {
        this.user = user;
        this.bookReview = bookReview;
        this.time = LocalDateTime.now();
    }

    public BookReviewLike(String hashCode, BookReview bookReview) {
        this.hashCode = hashCode;
        this.bookReview = bookReview;
        this.time = LocalDateTime.now();
    }
}