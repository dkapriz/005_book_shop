package com.example.bookshopapp.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "data model of transactional dto")
public class TransactionalDto {
    private String time;
    @JsonIgnore
    private LocalDateTime localDateTime;
    private Integer value;
    private String description;
}
