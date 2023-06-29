package com.example.bookshopapp.service;

import com.example.bookshopapp.api.dto.RegistrationForm;
import com.example.bookshopapp.config.BookShopConfig;
import com.example.bookshopapp.config.LanguageMessage;
import com.example.bookshopapp.exception.CheckCodeException;
import com.example.bookshopapp.exception.SendCodeException;
import com.example.bookshopapp.exception.SendSMSException;
import com.example.bookshopapp.model.User;
import com.example.bookshopapp.model.UserContact;
import com.example.bookshopapp.model.enums.ContactType;
import com.example.bookshopapp.repositories.UserContactRepository;
import com.example.bookshopapp.repositories.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import javassist.NotFoundException;
import javassist.tools.reflect.CannotCreateException;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@SpringBootTest
class AuthServiceTest {

    public static final String CODE = "111-111";
    public static final String TEST_EMAIL = "test@mail.ru";
    public static final String TEST_PHONE = "79991112233";
    public static final String TEST_USERNAME = "TestUser";

    private final AuthService authService;
    private final BookShopConfig config;
    private RegistrationForm registrationForm;
    @MockBean
    private UserRepository userRepositoryMock;
    @MockBean
    private UserContactRepository userContactRepositoryMock;
    @MockBean
    private SMSService smsServiceMock;
    @MockBean
    private MailService mailServiceMock;

    @Autowired
    AuthServiceTest(AuthService authService, BookShopConfig config) {
        this.authService = authService;
        this.config = config;
    }

    @BeforeEach
    void setUp() {
        registrationForm = new RegistrationForm();
        registrationForm.setName(TEST_USERNAME);
        registrationForm.setEmail(TEST_EMAIL);
        registrationForm.setPhone(TEST_PHONE);

        when(userRepositoryMock.findById(BookShopConfig.SERVICE_USER_ID)).thenReturn(Optional.of(getTestServiceUser()));
        when(userRepositoryMock.save(Mockito.any(User.class)))
                .thenAnswer(invocationOnMock -> invocationOnMock.getArguments()[0]);
        when(userContactRepositoryMock.save(Mockito.any(UserContact.class)))
                .thenAnswer(invocationOnMock -> invocationOnMock.getArguments()[0]);
    }

    @AfterEach
    void tearDown() {
        registrationForm = null;
    }

    @Test
    void saveNewContactTest() throws NotFoundException {
        UserContact userContact = authService
                .saveNewContact(TEST_EMAIL, ContactType.EMAIL, CODE);

        assertNotNull(userContact);
        assertTrue(CoreMatchers.is(userContact.getContact()).matches(TEST_EMAIL));
        assertTrue(CoreMatchers.is(userContact.getType()).matches(ContactType.EMAIL));
        assertTrue(CoreMatchers.is(userContact.getCode()).matches(CODE));
        assertTrue(CoreMatchers.is(userContact.getApproved()).matches(BookShopConfig.NOT_APPROVE_CONTACT));
        Mockito.verify(userContactRepositoryMock, times(1)).save(Mockito.any(UserContact.class));
    }

    @Test
    void registerNewUser() throws CannotCreateException {
        when(userRepositoryMock.findUserByApprovedEmail(registrationForm.getEmail()))
                .thenReturn(Optional.of(getTestServiceUser()));
        when(userRepositoryMock.findUserByApprovedPhoneNumber(registrationForm.getPhone()))
                .thenReturn(Optional.of(getTestServiceUser()));
        when(userContactRepositoryMock.findByContact(Mockito.any()))
                .thenReturn(Optional.of(getContact(TEST_EMAIL, ContactType.EMAIL,
                        BookShopConfig.NOT_APPROVE_CONTACT, 0, 0, getTestServiceUser())));
        User user = authService.registerNewUser(registrationForm);

        assertNotNull(user);
        assertTrue(CoreMatchers.is(user.getName()).matches(TEST_USERNAME));
        Mockito.verify(userContactRepositoryMock, times(2)).save(Mockito.any(UserContact.class));
        Mockito.verify(userRepositoryMock, times(1)).save(Mockito.any(User.class));
    }

    @Test
    void approveContact() throws CheckCodeException {
        when(userContactRepositoryMock.findByContact(TEST_EMAIL))
                .thenReturn(Optional.of(getContact(TEST_EMAIL, ContactType.EMAIL,
                        BookShopConfig.NOT_APPROVE_CONTACT, 0, 0, getTestServiceUser())));
        Integer id = authService.approveContact(TEST_EMAIL, CODE);

        assertNotNull(id);
        assertEquals(1, (int) id);
        Mockito.verify(userContactRepositoryMock, times(2)).save(Mockito.any(UserContact.class));
    }

    @Test
    void approveContactCodeException() {
        when(userContactRepositoryMock.findByContact(TEST_EMAIL))
                .thenReturn(Optional.of(getContact(TEST_EMAIL, ContactType.EMAIL,
                        BookShopConfig.NOT_APPROVE_CONTACT, 0, 0, getTestServiceUser())));

        CheckCodeException ex = assertThrows(CheckCodeException.class, () -> authService
                .approveContact(TEST_EMAIL, "222-222"));
        assertTrue(CoreMatchers.is(ex.getMessage()).matches(LanguageMessage.getExMsgCodeIsWrong()));
    }

    @Test
    void approveContactTimeoutException() {
        when(userContactRepositoryMock.findByContact(TEST_EMAIL))
                .thenReturn(Optional.of(getContact(TEST_EMAIL, ContactType.EMAIL, BookShopConfig.NOT_APPROVE_CONTACT,
                        0, config.getCodeExpiredTime(), getTestServiceUser())));

        CheckCodeException ex = assertThrows(CheckCodeException.class, () -> authService
                .approveContact(TEST_EMAIL, CODE));
        assertTrue(CoreMatchers.is(ex.getMessage()).matches(LanguageMessage.getExMsgCodeIsExpired()));
    }

    @Test
    void approveContactTrialsException() {
        when(userContactRepositoryMock.findByContact(TEST_EMAIL))
                .thenReturn(Optional.of(getContact(TEST_EMAIL, ContactType.EMAIL, BookShopConfig.NOT_APPROVE_CONTACT,
                        config.getCodeMaxTrialsEntry(), 0, getTestServiceUser())));

        CheckCodeException ex = assertThrows(CheckCodeException.class, () -> authService
                .approveContact(TEST_EMAIL, CODE));
        assertTrue(CoreMatchers.is(ex.getMessage()).matches(LanguageMessage.getExMsgCodeIsExceededCountTrialsValue()));
    }

    @Test
    void loginContactConfirmationPhone() throws SendSMSException, SendCodeException, NoSuchAlgorithmException,
            JsonProcessingException {
        when(userContactRepositoryMock.findByContact(TEST_PHONE))
                .thenReturn(Optional.of(getContact(TEST_PHONE, ContactType.PHONE,
                        BookShopConfig.APPROVE_CONTACT, 0, 5, getTestUser(TEST_USERNAME, 5))));
        authService.loginContactConfirmation(TEST_PHONE);

        Mockito.verify(smsServiceMock, times(1)).sendSMS(TEST_PHONE, CODE);
    }

    @Test
    void loginContactConfirmationEmail() throws SendSMSException, SendCodeException, NoSuchAlgorithmException,
            JsonProcessingException {
        when(userContactRepositoryMock.findByContact(TEST_EMAIL))
                .thenReturn(Optional.of(getContact(TEST_EMAIL, ContactType.EMAIL,
                        BookShopConfig.APPROVE_CONTACT, 0, 5, getTestUser(TEST_USERNAME, 5))));
        authService.loginContactConfirmation(TEST_EMAIL);

        Mockito.verify(mailServiceMock, times(1)).sendMailCode(TEST_EMAIL, CODE);
    }

    @Test
    void loginContactConfirmationTimeoutException() {
        when(userContactRepositoryMock.findByContact(TEST_EMAIL))
                .thenReturn(Optional.of(getContact(TEST_EMAIL, ContactType.EMAIL,
                        BookShopConfig.APPROVE_CONTACT, 0, 0, getTestUser(TEST_USERNAME, 5))));

        SendCodeException ex = assertThrows(SendCodeException.class, () -> authService
                .loginContactConfirmation(TEST_EMAIL));
        assertTrue(CoreMatchers.is(ex.getMessage()).matches(LanguageMessage.getExMsgCodeTimeout(4)));
    }

    @Test
    void loginContactConfirmationTrialsException() {
        when(userContactRepositoryMock.findByContact(TEST_EMAIL))
                .thenReturn(Optional.of(getContact(TEST_EMAIL, ContactType.EMAIL,
                        BookShopConfig.APPROVE_CONTACT, config.getCodeMaxTrialsEntry() + 1, 8,
                        getTestUser(TEST_USERNAME, 5))));

        SendCodeException ex = assertThrows(SendCodeException.class, () -> authService
                .loginContactConfirmation(TEST_EMAIL));
        assertTrue(CoreMatchers.is(ex.getMessage()).matches(LanguageMessage.getExMsgCodeTrials(1)));
    }

    @Test
    void loginContactConfirmationNotFoundException() {
        when(userContactRepositoryMock.findByContact(TEST_EMAIL))
                .thenReturn(Optional.of(getContact(TEST_EMAIL, ContactType.EMAIL,
                        BookShopConfig.APPROVE_CONTACT, 0, 0,
                        getTestServiceUser())));

        SendCodeException ex = assertThrows(SendCodeException.class, () -> authService
                .loginContactConfirmation(TEST_EMAIL));
        assertTrue(CoreMatchers.is(ex.getMessage()).matches(LanguageMessage.getExMsgContactNotFound()));
    }

    @Test
    void registerContactConfirmation() throws SendSMSException, SendCodeException, NotFoundException,
            NoSuchAlgorithmException, JsonProcessingException {
        when(userContactRepositoryMock.findByContact(TEST_PHONE))
                .thenReturn(Optional.of(getContact(TEST_PHONE, ContactType.PHONE,
                        BookShopConfig.APPROVE_CONTACT, 0, 5, getTestServiceUser())));
        authService.registerContactConfirmation(TEST_PHONE);

        Mockito.verify(smsServiceMock, times(1)).sendSMS(TEST_PHONE, CODE);
    }

    @Test
    void registerContactConfirmationRegistrationException() {
        when(userContactRepositoryMock.findByContact(TEST_PHONE))
                .thenReturn(Optional.of(getContact(TEST_PHONE, ContactType.PHONE,
                        BookShopConfig.APPROVE_CONTACT, 0, 5, getTestUser(TEST_USERNAME, 10))));

        SendCodeException ex = assertThrows(SendCodeException.class, () -> authService
                .registerContactConfirmation(TEST_PHONE));
        assertTrue(CoreMatchers.is(ex.getMessage()).matches(LanguageMessage.getExMsgContactIsRegistered()));
    }

    private User getTestServiceUser() {
        return getTestUser("Service user", BookShopConfig.SERVICE_USER_ID);
    }

    private User getTestUser(String name, int id) {
        User user = new User();
        user.setName(name);
        user.setId(id);
        return user;
    }

    private UserContact getContact(String contact, ContactType type, byte approve,
                                   int trials, long minutes, User user) {
        UserContact userContact = new UserContact();
        userContact.setId(10);
        userContact.setContact(contact);
        userContact.setType(type);
        userContact.setApproved(approve);
        userContact.setCode(CODE);
        userContact.setCodeTrials(trials);
        userContact.setCodeTime(LocalDateTime.now().minusMinutes(minutes).minusSeconds(10));
        userContact.setUser(user);
        return userContact;
    }
}