package com.example.bookshopapp.model;

import com.example.bookshopapp.model.compositekey.BookAuthorId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@EqualsAndHashCode
@Entity
@ApiModel(description = "data model of Book2Author entity")
public class Book2Author {

    @EmbeddedId
    private BookAuthorId bookAuthorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("bookId")
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("authorId")
    private Author author;

    @Column(name = "sort_index", columnDefinition = "INT DEFAULT 0", nullable = false)
    @ApiModelProperty("serial number of the author")
    private Integer sortIndex;
}
