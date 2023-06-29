package com.example.bookshopapp.api.response;


import com.example.bookshopapp.api.dto.TransactionalDto;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "data model of transactional response")
public class TransactionalListResponse {
    private Integer count;
    private List<TransactionalDto> transactions;
}
