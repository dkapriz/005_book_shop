package com.example.bookshopapp.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookDto {
    private Integer id;
    private String slug;
    private String image;
    private String authors;
    @JsonIgnore
    private String authorSlug;
    private String title;
    private Integer discount;
    private Boolean isBestseller;
    @JsonIgnore
    private Integer rating;
    private String status;
    private Integer price;
    private Integer discountPrice;
    @JsonIgnore
    private String description;

    @JsonProperty("rating")
    public String getRatingStr() {
        if (rating == 0) {
            return "false";
        }
        return String.valueOf(rating);
    }
}
