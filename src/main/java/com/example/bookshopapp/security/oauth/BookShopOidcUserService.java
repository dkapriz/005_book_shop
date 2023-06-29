package com.example.bookshopapp.security.oauth;

import com.example.bookshopapp.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class BookShopOidcUserService extends OidcUserService {
    private final AuthService authService;

    @Autowired
    public BookShopOidcUserService(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);
        Map<String, Object> attributes = oidcUser.getAttributes();
        authService.oAuthPostLogin((String) attributes.get("email"), (String) attributes.get("name"));
        return oidcUser;
    }
}
