package com.example.bookshopapp.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@ApiModel(description = "data model of faq entity")
public class Faq {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty("id generated by db automatically")
    private Integer id;

    @Column(name = "sort_index", columnDefinition = "INT DEFAULT 0", nullable = false)
    @ApiModelProperty("the sequential number of the question in the list of questions on the Help page")
    private Integer sortIndex;

    @Column(columnDefinition = "VARCHAR(255)", nullable = false)
    @ApiModelProperty("question")
    private String question;

    @Column(columnDefinition = "TEXT", nullable = false)
    @ApiModelProperty("response in HTML format")
    private String answer;
}
