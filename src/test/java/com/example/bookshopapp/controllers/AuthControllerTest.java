package com.example.bookshopapp.controllers;

import com.example.bookshopapp.config.BookShopConfig;
import com.example.bookshopapp.model.JWTBlackList;
import com.example.bookshopapp.model.User;
import com.example.bookshopapp.repositories.JWTBlackListRepository;
import com.example.bookshopapp.repositories.UserRepository;
import com.example.bookshopapp.security.code.BookShopUserDetails;
import com.example.bookshopapp.security.jwt.JWTUtil;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

import javax.servlet.http.Cookie;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/application-test.yaml")
@Sql(value = {"/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = {"/delete-all.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class AuthControllerTest {
    public static final String CONTACT_USER_AUTH = "test@mail.ru";
    public static final String TEST_CONTACT_EMAIL_APPROVE_SERVICE_USER = "testemail@mail.ru";
    public static final String TEST_CONTACT_PHONE_APPROVE_SERVICE_USER = "+79991112233";
    public static final String TEST_CONTACT_PHONE_BAD_PHONE = "+79991112210";
    public static final String TEST_NAME = "Test-name";
    private final MockMvc mockMvc;
    private final UserRepository userRepository;
    private final JWTUtil jwtUtil;
    private final JWTBlackListRepository jwtBlackListRepository;

    @Autowired
    AuthControllerTest(MockMvc mockMvc, UserRepository userRepository, JWTUtil jwtUtil,
                       JWTBlackListRepository jwtBlackListRepository) {
        this.mockMvc = mockMvc;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.jwtBlackListRepository = jwtBlackListRepository;
    }

    @Test
    void handleUserRegistration() throws Exception {
        RequestBuilder requestBuilder = post("/reg").contentType(MediaType.APPLICATION_FORM_URLENCODED).
                content(EntityUtils.toString(new UrlEncodedFormEntity(Arrays.asList(
                        new BasicNameValuePair("name", TEST_NAME),
                        new BasicNameValuePair("phone", TEST_CONTACT_PHONE_APPROVE_SERVICE_USER),
                        new BasicNameValuePair("email", TEST_CONTACT_EMAIL_APPROVE_SERVICE_USER)
                ))));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpect(status().isOk());
        Optional<User> user = userRepository.findUserByApprovedEmail(TEST_CONTACT_EMAIL_APPROVE_SERVICE_USER);
        assertTrue(user.isPresent());
        assertNotEquals(BookShopConfig.SERVICE_USER_ID, user.get().getId());
    }

    @Test
    void handleUserRegistrationException() throws Exception {
        RequestBuilder requestBuilder = post("/reg").contentType(MediaType.APPLICATION_FORM_URLENCODED).
                content(EntityUtils.toString(new UrlEncodedFormEntity(Arrays.asList(
                        new BasicNameValuePair("name", TEST_NAME),
                        new BasicNameValuePair("phone", TEST_CONTACT_PHONE_BAD_PHONE),
                        new BasicNameValuePair("email", TEST_CONTACT_EMAIL_APPROVE_SERVICE_USER)
                ))));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @WithUserDetails(CONTACT_USER_AUTH)
    void logoutTest() throws Exception {
        UserDetails userDetails = (BookShopUserDetails) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        String token = jwtUtil.generateToken(userDetails);
        mockMvc.perform(get("/logout").cookie(new Cookie("token", token)))
                .andDo(print())
                .andExpect(unauthenticated())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/signin"));
        List<JWTBlackList> jwtBlackList = jwtBlackListRepository.findAll();
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(1, jwtBlackList.size());
        assertEquals(token, jwtBlackList.get(0).getToken());
    }
}