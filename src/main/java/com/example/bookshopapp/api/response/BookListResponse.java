package com.example.bookshopapp.api.response;

import com.example.bookshopapp.api.dto.BookDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "data model of book DTO")
public class BookListResponse {
    @ApiModelProperty("list of books with paginated output")
    private List<BookDto> books;
    @ApiModelProperty("total number of books found on request")
    private Long count;

    public BookListResponse(List<BookDto> books) {
        this.books = books;
        count = (long) books.size();
    }
}