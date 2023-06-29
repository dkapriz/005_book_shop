package com.example.bookshopapp.model;

import com.example.bookshopapp.model.compositekey.BookUserId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode
@Entity
@Table(name = "book2user_recently_view")
@ApiModel(description = "data model of Book2UserRecentlyView entity")
public class Book2UserRecentlyView {

    @EmbeddedId
    private BookUserId bookUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("bookId")
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    private User user;

    @Column(columnDefinition = "TIMESTAMP", nullable = false)
    @ApiModelProperty("date and time when the binding occurred")
    private LocalDateTime time;
}
