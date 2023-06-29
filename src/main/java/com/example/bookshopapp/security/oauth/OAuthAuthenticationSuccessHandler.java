package com.example.bookshopapp.security.oauth;

import com.example.bookshopapp.config.BookShopConfig;
import com.example.bookshopapp.security.jwt.JWTUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Slf4j
public class OAuthAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private JWTUtil jwtUtil;
    private BookShopConfig config;

    @Autowired
    public void setJwtUtil(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Autowired
    public void setConfig(BookShopConfig config) {
        this.config = config;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                        Authentication authentication) throws IOException {
        if (httpServletResponse.isCommitted()) {
            return;
        }
        if (authentication.getPrincipal() instanceof DefaultOidcUser) {
            DefaultOidcUser oidcUser = (DefaultOidcUser) authentication.getPrincipal();
            log.info("the user " + oidcUser.getEmail() + " is logged in");
            String token = jwtUtil.generateToken(oidcUser);

            ResponseCookie cookie = ResponseCookie.from(BookShopConfig.TOKEN_COOKIE_NAME, token)
                    .httpOnly(true).secure(true).path("/").sameSite("Lax").build();
            httpServletResponse.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());

            String redirectionUri = UriComponentsBuilder.fromUriString(config.getOauthRedirectURI())
                    .build().toUriString();
            getRedirectStrategy().sendRedirect(httpServletRequest, httpServletResponse, redirectionUri);
        }
    }
}
