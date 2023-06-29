package com.example.bookshopapp.controllers;

import com.example.bookshopapp.api.request.ChangeBookStatusRequest;
import com.example.bookshopapp.config.BookShopConfig;
import com.example.bookshopapp.model.Book2User;
import com.example.bookshopapp.repositories.Book2UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/application-test.yaml")
@Sql(value = {"/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = {"/delete-all.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class GeneralRestApiControllerTest {

    public static final int TEST_BOOK_ID = 25;
    public static final String TEST_BOOK_SLUG = "book-hrf-593";
    public static final int TEST_BOOK_CART_ID = 5;
    public static final int TEST_USER_ID = 2;
    public static final String CONTACT = "test@mail.ru";
    private static final Integer TEST_BOOK_FAIL_ID = 100;
    private final MockMvc mockMvc;
    private final Book2UserRepository book2UserRepository;

    @Autowired
    GeneralRestApiControllerTest(MockMvc mockMvc, Book2UserRepository book2UserRepository) {
        this.mockMvc = mockMvc;
        this.book2UserRepository = book2UserRepository;
    }

    @Test
    void handleChangeBookStatusAddToCartNoAuth() throws Exception {
        ChangeBookStatusRequest request = new ChangeBookStatusRequest();
        request.setStatus("CART");
        request.setBooksIds(Collections.singletonList(TEST_BOOK_ID));

        ObjectMapper mapper = new ObjectMapper();
        RequestBuilder requestBuilder = post("/api/changeBookStatus").contentType(MediaType.APPLICATION_JSON).
                content(mapper.writeValueAsBytes(request));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result").value("true"))
                .andExpect(cookie().exists(BookShopConfig.CART_COOKIE_NAME))
                .andExpect(cookie().value(BookShopConfig.CART_COOKIE_NAME, TEST_BOOK_SLUG));
    }

    @Test
    @WithUserDetails(CONTACT)
    void handleChangeBookStatusAddToCart() throws Exception {
        ChangeBookStatusRequest request = new ChangeBookStatusRequest();
        request.setStatus("CART");
        request.setBooksIds(Collections.singletonList(TEST_BOOK_ID));

        ObjectMapper mapper = new ObjectMapper();
        RequestBuilder requestBuilder = post("/api/changeBookStatus").contentType(MediaType.APPLICATION_JSON).
                content(mapper.writeValueAsBytes(request));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result").value("true"));
        Optional<Book2User> book2User = book2UserRepository.getBook2UserByBookAndUserId(TEST_BOOK_ID,
                TEST_USER_ID);
        assertTrue(book2User.isPresent());
        assertEquals("CART", book2User.get().getBook2UserType().getCode());
    }

    @Test
    @WithUserDetails(CONTACT)
    void handleChangeBookStatusDeleteFromCart() throws Exception {
        ChangeBookStatusRequest request = new ChangeBookStatusRequest();
        request.setStatus("UNLINK");
        request.setBooksIds(Collections.singletonList(TEST_BOOK_CART_ID));

        ObjectMapper mapper = new ObjectMapper();
        RequestBuilder requestBuilder = post("/api/changeBookStatus").contentType(MediaType.APPLICATION_JSON).
                content(mapper.writeValueAsBytes(request));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result").value("true"));
        Optional<Book2User> book2User = book2UserRepository.getBook2UserByBookAndUserId(TEST_BOOK_CART_ID,
                TEST_USER_ID);
        assertFalse(book2User.isPresent());
    }

    @Test
    @WithUserDetails(CONTACT)
    void handleChangeBookStatusMoveFromCartToKept() throws Exception {
        ChangeBookStatusRequest request = new ChangeBookStatusRequest();
        request.setStatus("KEPT");
        request.setBooksIds(Collections.singletonList(TEST_BOOK_CART_ID));

        ObjectMapper mapper = new ObjectMapper();
        RequestBuilder requestBuilder = post("/api/changeBookStatus").contentType(MediaType.APPLICATION_JSON).
                content(mapper.writeValueAsBytes(request));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result").value("true"));
        Optional<Book2User> book2User = book2UserRepository.getBook2UserByBookAndUserId(TEST_BOOK_CART_ID,
                TEST_USER_ID);
        assertTrue(book2User.isPresent());
        assertEquals("KEPT", book2User.get().getBook2UserType().getCode());
    }

    @Test
    @WithUserDetails(CONTACT)
    void handleChangeBookStatusFailBookId() throws Exception {
        ChangeBookStatusRequest request = new ChangeBookStatusRequest();
        request.setStatus("KEPT");
        request.setBooksIds(Collections.singletonList(TEST_BOOK_FAIL_ID));

        ObjectMapper mapper = new ObjectMapper();
        RequestBuilder requestBuilder = post("/api/changeBookStatus").contentType(MediaType.APPLICATION_JSON).
                content(mapper.writeValueAsBytes(request));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result").value("false"));
    }
}