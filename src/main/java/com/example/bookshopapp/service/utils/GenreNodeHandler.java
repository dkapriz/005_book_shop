package com.example.bookshopapp.service.utils;

import com.example.bookshopapp.api.dto.GenreListDto;
import com.example.bookshopapp.model.Genre;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;

@AllArgsConstructor
public class GenreNodeHandler extends RecursiveTask<GenreListDto> {

    private transient List<Genre> genreList;
    private transient Genre currentGenre;

    public GenreNodeHandler(List<Genre> genreList) {
        this.genreList = genreList;
        currentGenre = null;
    }

    @Override
    protected GenreListDto compute() {
        GenreListDto result = new GenreListDto(currentGenre);
        int currentId = currentGenre == null ? 0 : currentGenre.getId();
        List<GenreNodeHandler> taskList = new ArrayList<>();
        int currentIndex = 0;
        for (Genre genre : genreList) {
            if (currentId == genre.getParentId()) {
                GenreNodeHandler task = new GenreNodeHandler(genreList.subList(currentIndex, genreList.size()), genre);
                task.fork();
                taskList.add(task);
            }
            currentIndex++;
        }
        for (GenreNodeHandler task : taskList) {
            result.getGenreList().add(task.join());
        }
        return result;
    }
}
