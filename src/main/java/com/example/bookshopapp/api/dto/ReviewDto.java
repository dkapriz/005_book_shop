package com.example.bookshopapp.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDto {
    private Integer id;
    private String userName;
    private String timeStr;
    private String textShort;
    private String textExtension;
    private Integer rating;
    private Integer likeCount;
    private Integer dislikeCount;
}
