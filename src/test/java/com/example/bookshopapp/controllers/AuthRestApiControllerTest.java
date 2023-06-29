package com.example.bookshopapp.controllers;

import com.example.bookshopapp.api.request.ContactConfirmationPayload;
import com.example.bookshopapp.config.BookShopConfig;
import com.example.bookshopapp.config.LanguageMessage;
import com.example.bookshopapp.service.MailService;
import com.example.bookshopapp.service.SMSService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/application-test.yaml")
@Sql(value = {"/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = {"/delete-all.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class AuthRestApiControllerTest {
    public static final String TEST_CONTACT_EMAIL_NOT_APPROVE_SERVICE_USER = "testNotApprove@mail.ru";
    public static final String TEST_CONTACT_EMAIL_TRIALS = "testTrials@mail.ru";
    public static final String TEST_CONTACT_EMAIL_EXPIRED = "testExpired@mail.ru";
    public static final String TEST_CONTACT_EMAIL_REG_CONFIRM = "testRegConfirm@mail.ru";
    public static final String TEST_CONTACT_PHONE_REG_CONFIRM = "79991234567";
    public static final String TEST_CONTACT_EMAIL_LOGIN_CONFIRM = "testLoginConfirm@mail.ru";
    public static final String TEST_CONTACT_PHONE_LOGIN_CONFIRM = "79995556644";
    public static final String CONTACT_USER_AUTH = "test@mail.ru";
    public static final String TEST_CODE = "777 777";
    public static final String TEST_BAD_CODE = "777 888";

    private final MockMvc mockMvc;
    @MockBean
    private SMSService smsService;
    @MockBean
    private MailService mailService;

    @Autowired
    AuthRestApiControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void handleRequestContactConfirmationMail() throws Exception {
        ContactConfirmationPayload contactConfirmationPayload = new ContactConfirmationPayload();
        contactConfirmationPayload.setContact(TEST_CONTACT_EMAIL_REG_CONFIRM);
        ObjectMapper mapper = new ObjectMapper();
        RequestBuilder requestBuilder = post("/api/requestContactConfirmation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(contactConfirmationPayload));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result").value("true"));
        Mockito.verify(mailService, times(1)).sendMailCode(any(), any());
    }

    @Test
    void handleRequestContactConfirmationPhone() throws Exception {
        ContactConfirmationPayload contactConfirmationPayload = new ContactConfirmationPayload();
        contactConfirmationPayload.setContact(TEST_CONTACT_PHONE_REG_CONFIRM);
        ObjectMapper mapper = new ObjectMapper();
        RequestBuilder requestBuilder = post("/api/requestContactConfirmation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(contactConfirmationPayload));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result").value("true"));
        Mockito.verify(smsService, times(1)).sendSMS(any(), any());
    }

    @Test
    void handleRequestLoginContactConfirmationMail() throws Exception {
        ContactConfirmationPayload contactConfirmationPayload = new ContactConfirmationPayload();
        contactConfirmationPayload.setContact(TEST_CONTACT_EMAIL_LOGIN_CONFIRM);
        ObjectMapper mapper = new ObjectMapper();
        RequestBuilder requestBuilder = post("/api/requestLoginContactConfirmation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(contactConfirmationPayload));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result").value("true"));
        Mockito.verify(mailService, times(1)).sendMailCode(any(), any());
    }

    @Test
    void handleRequestLoginContactConfirmationPhone() throws Exception {
        ContactConfirmationPayload contactConfirmationPayload = new ContactConfirmationPayload();
        contactConfirmationPayload.setContact(TEST_CONTACT_PHONE_LOGIN_CONFIRM);
        ObjectMapper mapper = new ObjectMapper();
        RequestBuilder requestBuilder = post("/api/requestLoginContactConfirmation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(contactConfirmationPayload));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result").value("true"));
        Mockito.verify(smsService, times(1)).sendSMS(any(), any());
    }

    @Test
    void handleApproveContactRegister() throws Exception {
        ContactConfirmationPayload contactConfirmationPayload = new ContactConfirmationPayload();
        contactConfirmationPayload.setContact(TEST_CONTACT_EMAIL_NOT_APPROVE_SERVICE_USER);
        contactConfirmationPayload.setCode(TEST_CODE);
        ObjectMapper mapper = new ObjectMapper();
        RequestBuilder requestBuilder = post("/api/approveContact").contentType(MediaType.APPLICATION_JSON).
                content(mapper.writeValueAsBytes(contactConfirmationPayload));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(unauthenticated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result").value("true"));
    }

    @Test
    void handleApproveContactLogin() throws Exception {
        ContactConfirmationPayload contactConfirmationPayload = new ContactConfirmationPayload();
        contactConfirmationPayload.setContact(CONTACT_USER_AUTH);
        contactConfirmationPayload.setCode(TEST_CODE);
        ObjectMapper mapper = new ObjectMapper();
        RequestBuilder requestBuilder = post("/api/approveContact").contentType(MediaType.APPLICATION_JSON).
                content(mapper.writeValueAsBytes(contactConfirmationPayload));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(cookie().exists(BookShopConfig.TOKEN_COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result").value("true"));
    }

    @Test
    void handleApproveContactWrongCode() throws Exception {
        ContactConfirmationPayload contactConfirmationPayload = new ContactConfirmationPayload();
        contactConfirmationPayload.setContact(TEST_CONTACT_EMAIL_NOT_APPROVE_SERVICE_USER);
        contactConfirmationPayload.setCode(TEST_BAD_CODE);
        ObjectMapper mapper = new ObjectMapper();
        RequestBuilder requestBuilder = post("/api/approveContact").contentType(MediaType.APPLICATION_JSON).
                content(mapper.writeValueAsBytes(contactConfirmationPayload));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(unauthenticated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result").value("false"))
                .andExpect(jsonPath("$.error").value(LanguageMessage.EX_MSG_CODE_IS_WRONG_EN));
    }

    @Test
    void handleApproveContactExceedTrials() throws Exception {
        ContactConfirmationPayload contactConfirmationPayload = new ContactConfirmationPayload();
        contactConfirmationPayload.setContact(TEST_CONTACT_EMAIL_TRIALS);
        contactConfirmationPayload.setCode(TEST_CODE);
        ObjectMapper mapper = new ObjectMapper();
        RequestBuilder requestBuilder = post("/api/approveContact").contentType(MediaType.APPLICATION_JSON).
                content(mapper.writeValueAsBytes(contactConfirmationPayload));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(unauthenticated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result").value("false"))
                .andExpect(jsonPath("$.error")
                        .value(LanguageMessage.EX_MSG_CODE_IS_EXCEEDED_COUNT_TRIALS_VALUE_EN));
    }

    @Test
    void handleApproveContactExpiredCode() throws Exception {
        ContactConfirmationPayload contactConfirmationPayload = new ContactConfirmationPayload();
        contactConfirmationPayload.setContact(TEST_CONTACT_EMAIL_EXPIRED);
        contactConfirmationPayload.setCode(TEST_CODE);
        ObjectMapper mapper = new ObjectMapper();
        RequestBuilder requestBuilder = post("/api/approveContact").contentType(MediaType.APPLICATION_JSON).
                content(mapper.writeValueAsBytes(contactConfirmationPayload));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(unauthenticated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result").value("false"))
                .andExpect(jsonPath("$.error").value(LanguageMessage.EX_MSG_CODE_IS_EXPIRED_EN));
    }
}