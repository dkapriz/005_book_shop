package com.example.bookshopapp.api.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RedirectResponse extends ResultResponse {
    private boolean redirect;
    private String redirectUri;

    public RedirectResponse(boolean result, boolean redirect, String redirectUri) {
        super(result);
        this.redirect = redirect;
        this.redirectUri = redirectUri;
    }

    public RedirectResponse(String error, boolean redirect, String redirectUri) {
        super(error);
        this.redirect = redirect;
        this.redirectUri = redirectUri;
    }
}
