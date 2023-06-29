package com.example.bookshopapp.security.code;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

@Getter
@Setter
public class UserContactAuthenticationProvider implements AuthenticationProvider {

    private UserDetailsService userDetailsService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        UserContactAuthenticationToken authenticationToken = (UserContactAuthenticationToken) authentication;
        UserDetails userDetails = userDetailsService.loadUserByUsername((String) authenticationToken.getPrincipal());
        if (userDetails == null) {
            throw new InternalAuthenticationServiceException("UserDetails not found");
        }
        UserContactAuthenticationToken resultAuthenticationToken = new UserContactAuthenticationToken(userDetails,
                userDetails.getAuthorities());
        resultAuthenticationToken.setDetails(authenticationToken.getDetails());
        return resultAuthenticationToken;
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return UserContactAuthenticationToken.class.isAssignableFrom(aClass);
    }
}
