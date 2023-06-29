package com.example.bookshopapp.service;

import com.example.bookshopapp.api.request.ContactConfirmationPayload;
import com.example.bookshopapp.config.BookShopConfig;
import com.example.bookshopapp.config.LanguageMessage;
import com.example.bookshopapp.model.UserContact;
import com.example.bookshopapp.model.enums.ContactType;
import com.example.bookshopapp.repositories.UserContactRepository;
import com.example.bookshopapp.security.jwt.JWTUtil;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@SpringBootTest
class LoginServiceTest {
    public static final String CODE = "111-111";
    public static final String TEST_EMAIL = "test@mail.ru";
    public static final String TEST_EMAIL_NOT_FOUND = "test_not_found@mail.ru";
    private final LoginService loginService;
    private final JWTUtil jwtUtil;
    private ContactConfirmationPayload payload;
    @MockBean
    private UserContactRepository userContactRepositoryMock;

    @Autowired
    LoginServiceTest(LoginService loginService, JWTUtil jwtUtil) {
        this.loginService = loginService;
        this.jwtUtil = jwtUtil;
    }

    @BeforeEach
    void setUp() {
        payload = new ContactConfirmationPayload();
        payload.setCode(CODE);
        payload.setContact(TEST_EMAIL);

        UserContact userContact = new UserContact();
        userContact.setContact(TEST_EMAIL);
        userContact.setType(ContactType.EMAIL);
        userContact.setId(10);
        userContact.setApproved(BookShopConfig.APPROVE_CONTACT);
        when(userContactRepositoryMock.findByContact(TEST_EMAIL)).thenReturn(Optional.of(userContact));
    }

    @AfterEach
    void tearDown() {
        payload = null;
    }

    @Test
    void jwtLogin() {
        String jwtToken = loginService.jwtLogin(payload);

        assertNotNull(jwtToken);
        assertFalse(jwtUtil.isTokenExpired(jwtToken));
        assertTrue(CoreMatchers.is(jwtUtil.extractUsername(jwtToken)).matches(TEST_EMAIL));
        Mockito.verify(userContactRepositoryMock, times(2)).findByContact(TEST_EMAIL);
    }

    @Test
    void jwtLoginUserNotFound() {
        payload.setContact(TEST_EMAIL_NOT_FOUND);
        UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class, () -> loginService
                .jwtLogin(payload));
        assertTrue(CoreMatchers.is(ex.getMessage()).matches(LanguageMessage.EX_MSG_USER_NOT_FOUND));
    }
}