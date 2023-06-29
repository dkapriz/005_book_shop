package com.example.bookshopapp.api.dto;

import com.example.bookshopapp.api.response.ResultResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.TreeSet;

@Getter
@Setter
public class BookFileListDto extends ResultResponse {
    private Set<BookFileDto> bookFileList;

    public BookFileListDto(boolean result) {
        super(result);
        bookFileList = new TreeSet<>();
    }

    public BookFileListDto(String errorMessage) {
        super(errorMessage);
        bookFileList = new TreeSet<>();
    }

    public BookFileListDto(boolean result, Set<BookFileDto> bookFileList) {
        super(result);
        this.bookFileList = bookFileList;
    }
}
