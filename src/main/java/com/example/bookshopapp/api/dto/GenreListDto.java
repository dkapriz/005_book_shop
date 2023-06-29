package com.example.bookshopapp.api.dto;

import com.example.bookshopapp.model.Genre;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class GenreListDto {
    private Genre genre;
    private List<GenreListDto> genreList;

    public GenreListDto() {
        genreList = new ArrayList<>();
    }

    public GenreListDto(Genre genre) {
        this.genre = genre;
        genreList = new ArrayList<>();
    }
}
