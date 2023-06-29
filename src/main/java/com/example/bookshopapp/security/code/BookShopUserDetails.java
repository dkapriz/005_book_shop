package com.example.bookshopapp.security.code;

import com.example.bookshopapp.model.User;
import com.example.bookshopapp.model.UserContact;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class BookShopUserDetails implements UserDetails {
    private final transient UserContact userContact;

    public BookShopUserDetails(UserContact userContact) {
        this.userContact = userContact;
    }

    public User getUser() {
        return userContact.getUser();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return userContact.getCode();
    }

    @Override
    public String getUsername() {
        return userContact.getContact();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
