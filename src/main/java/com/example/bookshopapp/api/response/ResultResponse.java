package com.example.bookshopapp.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResultResponse {
    protected Boolean result;
    @JsonProperty("error")
    protected String errorMessage;

    public ResultResponse(){
        result = null;
        errorMessage = null;
    }

    public ResultResponse(Boolean result) {
        this.result = result;
        errorMessage = null;
    }

    public ResultResponse(String errorMessage) {
        result = false;
        this.errorMessage = errorMessage;
    }
}
