package com.example.bookshopapp.security.code;

import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserContactAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    public static final String CONTACT_KEY = "contact";
    private String contactParameter = CONTACT_KEY;
    private boolean postOnly = true;

    public UserContactAuthenticationFilter() {
        super(new AntPathRequestMatcher("/login/contact", "POST"));
    }

    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws
            AuthenticationException {
        if (this.postOnly && !request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        } else {
            String contact = this.obtainContact(request);
            if (contact == null) {
                contact = "";
            }

            contact = contact.trim();
            UserContactAuthenticationToken authRequest = new UserContactAuthenticationToken(contact);
            this.setDetails(request, authRequest);
            return this.getAuthenticationManager().authenticate(authRequest);
        }
    }

    @Nullable
    protected String obtainContact(HttpServletRequest request) {
        return request.getParameter(this.contactParameter);
    }

    protected void setDetails(HttpServletRequest request, UserContactAuthenticationToken authRequest) {
        authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
    }

    public void setContactParameter(String contactParameter) {
        Assert.hasText(contactParameter, "Contact parameter must not be empty or null");
        this.contactParameter = contactParameter;
    }

    public void setPostOnly(boolean postOnly) {
        this.postOnly = postOnly;
    }

    public final String getContactParameter() {
        return this.contactParameter;
    }
}

