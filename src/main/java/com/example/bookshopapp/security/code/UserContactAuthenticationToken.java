package com.example.bookshopapp.security.code;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class UserContactAuthenticationToken extends AbstractAuthenticationToken {
    private final transient Object principal;

    public UserContactAuthenticationToken(String contact) {
        super(null);
        this.principal = contact;
        setAuthenticated(false);
    }

    public UserContactAuthenticationToken(Object principal, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        super.setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return this.principal;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        if (isAuthenticated) {
            throw new IllegalArgumentException("Cannot set this token to trusted - use constructor which takes a " +
                    "GrantedAuthority list instead");
        } else {
            super.setAuthenticated(false);
        }
    }
}
