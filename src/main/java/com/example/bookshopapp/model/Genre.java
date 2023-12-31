package com.example.bookshopapp.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@ApiModel(description = "data model of genre entity")
public class Genre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty("id generated by db automatically")
    private Integer id;

    @Column(columnDefinition = "VARCHAR(255)", nullable = false)
    @ApiModelProperty("name of the genre")
    private String name;

    @Column(name = "parent_id")
    @ApiModelProperty("id of the parent genre, or NULL if the genre is the root one")
    private Integer parentId;

    @NaturalId
    @Column(columnDefinition = "VARCHAR(255)", nullable = false, unique = true)
    @ApiModelProperty("the mnemonic code of the genre used in the links to the page of this genre")
    private String slug;

    @JsonIgnore
    @ManyToMany(mappedBy = "genres", fetch = FetchType.LAZY)
    private Set<Book> books;

    public Genre() {
        books = new HashSet<>();
    }

    public Genre(String name, Integer parentId, String slug) {
        this();
        this.name = name;
        this.parentId = parentId;
        this.slug = slug;
    }

    public Genre(String name, String slug) {
        this();
        this.name = name;
        this.parentId = 0;
        this.slug = slug;
    }

    @JsonGetter("bookCount")
    public Integer getBookCount(){
        return books.size();
    }
}