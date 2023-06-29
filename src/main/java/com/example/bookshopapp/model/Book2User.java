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
@ApiModel(description = "data model of Book2User entity")
public class Book2User {

    @EmbeddedId
    private BookUserId bookUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("bookId")
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    private User user;

    @OneToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "type_id", columnDefinition = "INT", nullable = false)
    private Book2UserType book2UserType;

    @Column(columnDefinition = "TIMESTAMP", nullable = false)
    @ApiModelProperty("date and time when the binding occurred")
    private LocalDateTime time;
}
