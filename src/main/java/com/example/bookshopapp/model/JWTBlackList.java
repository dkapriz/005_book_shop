package com.example.bookshopapp.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "jwt_black_list")
public class JWTBlackList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(columnDefinition = "VARCHAR(255)", nullable = false)
    private String token;
    @Column(nullable = false)
    private Date creation;
    @Column(nullable = false)
    private Date expiration;
}
