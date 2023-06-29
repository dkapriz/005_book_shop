package com.example.bookshopapp.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApproveContactResponse extends ResultResponse{
    @JsonProperty("return")
    private boolean returnParam;

    public ApproveContactResponse(boolean result, boolean returnParam){
        super(result);
        this.returnParam = returnParam;
    }

    public ApproveContactResponse(String error, boolean returnParam){
        super(error);
        this.returnParam = returnParam;
    }
}