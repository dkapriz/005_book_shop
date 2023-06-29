package com.example.bookshopapp.model;

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
@Table(name = "tags")
@ApiModel(description = "data model of book tag entity")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty("id generated by db automatically")
    private int id;

    @NaturalId
    @Column(columnDefinition = "VARCHAR(255)", nullable = false, unique = true)
    @ApiModelProperty("Unique tag name")
    private String name;

    @Column(columnDefinition = "FLOAT8 DEFAULT 0", nullable = false)
    @ApiModelProperty("Tag Weight")
    private Double weight;

    @NaturalId
    @Column(columnDefinition = "VARCHAR(255)", nullable = false, unique = true)
    @ApiModelProperty("mnemonic ID of the tag")
    private String slug;

    @JsonIgnore
    @ManyToMany(mappedBy = "tags", fetch = FetchType.EAGER)
    private Set<Book> books;

    public Tag() {
        books = new HashSet<>();
    }

    public Tag(String name) {
        this();
        this.name = name;
    }
}
