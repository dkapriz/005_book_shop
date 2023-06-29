package com.example.bookshopapp.service;

import com.example.bookshopapp.api.dto.ProfileUserDto;
import com.example.bookshopapp.api.dto.RegistrationForm;
import com.example.bookshopapp.api.response.ApproveContactResponse;
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
import lombok.extern.slf4j.Slf4j;
import org.hibernate.procedure.NoSuchParameterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PreDestroy;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.bookshopapp.config.BookShopConfig.*;
import static com.example.bookshopapp.config.LanguageMessage.EX_MSG_USER_NOT_FOUND;

@Service
@Slf4j
public class
AuthService {
    private final UserRepository userRepository;
    private final UserContactRepository userContactRepository;
    private final SMSService smsService;
    private final MailService mailService;
    private final BookShopConfig config;
    private static final Random random = new Random();
    private final List<UserContact> userContactAddedList;

    @Autowired
    public AuthService(UserRepository userRepository, UserContactRepository userContactRepository,
                       SMSService smsService, MailService mailService,
                       BookShopConfig config) {
        this.userRepository = userRepository;
        this.userContactRepository = userContactRepository;
        this.smsService = smsService;
        this.mailService = mailService;
        this.config = config;
        userContactAddedList = new ArrayList<>();
    }

    /**
     * Метод удаляет все добавленные, но не привязанные к пользователю контакты
     */
    @PreDestroy
    @Transactional
    public void deleteAddedUserContact() throws NotFoundException {
        if (!userContactAddedList.isEmpty()) {
            User serviceUser = getServiceUser();
            for (UserContact contact : new ArrayList<>(userContactAddedList)) {
                if(contact.getUser().equals(serviceUser)) {
                    UserContact curContact = serviceUser.getUserContact(contact.getContact());
                    serviceUser.removeContact(curContact);
                    userContactRepository.delete(curContact);
                }
                userContactAddedList.remove(contact);
            }
        }
    }

    /**
     * Метод получает текущего пользователя
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        String contact = authentication.getName();
        Optional<UserContact> userContact = userContactRepository.findByContact(contact);
        return userContact.map(UserContact::getUser).orElse(null);
    }

    public ProfileUserDto getProfileCurUser() {
        User user = getCurrentUser();
        if (user == null) {
            throw new UsernameNotFoundException(EX_MSG_USER_NOT_FOUND);
        }
        ProfileUserDto profileUserDto = new ProfileUserDto();
        profileUserDto.setName(user.getName());
        for (UserContact contact : user.getUserContacts()) {
            if (contact.getType().equals(ContactType.EMAIL)) {
                profileUserDto.setMail(contact.getContact());
                profileUserDto.setConfirmEmail(contact.getApproved() == APPROVE_CONTACT);
            }
            if (contact.getType().equals(ContactType.PHONE)) {
                profileUserDto.setPhone(contact.getContact());
                profileUserDto.setConfirmPhone(contact.getApproved() == APPROVE_CONTACT);
            }
        }
        return profileUserDto;
    }

    public void oAuthPostLogin(String contact, String name) {
        Optional<UserContact> userContact = userContactRepository.findByContact(contact);
        UserContact curUserContact = new UserContact();
        if (userContact.isPresent()) {
            curUserContact = userContact.get();
            if (curUserContact.getUser().getId() == BookShopConfig.SERVICE_USER_ID ||
                    curUserContact.getApproved() == BookShopConfig.NOT_APPROVE_CONTACT) {
                curUserContact.setApproved(BookShopConfig.APPROVE_CONTACT);
            } else {
                return;
            }
        } else {
            curUserContact.setApproved(BookShopConfig.APPROVE_CONTACT);
            curUserContact.setContact(contact);
            curUserContact.setType(ContactType.EMAIL);
        }
        User user = saveNewUser(name);
        curUserContact.setUser(user);
        userContactRepository.save(curUserContact);
    }

    /**
     * Метод регистрации нового пользователя
     */
    public User registerNewUser(RegistrationForm registrationForm) throws CannotCreateException {
        Optional<User> userByEmail = userRepository.findUserByApprovedEmail(registrationForm.getEmail());
        Optional<User> userByPhone =
                userRepository.findUserByApprovedPhoneNumber(clearPhoneNumber(registrationForm.getPhone()));
        if (userByEmail.isPresent() && userByPhone.isPresent() &&
                userByEmail.get().getId() == BookShopConfig.SERVICE_USER_ID &&
                userByPhone.get().getId() == BookShopConfig.SERVICE_USER_ID) {
            User user = saveNewUser(registrationForm.getName());
            setUserInContact(registrationForm.getEmail(), user);
            setUserInContact(registrationForm.getPhone(), user);
            return user;
        } else {
            throw new CannotCreateException("registerNewUser - the user has not been added to the database");
        }
    }

    /**
     * Метод обновляет контакты пользователя
     *
     * @param profileUserDto - параметры отправленной формы
     * @return true в случае успешного выполнения
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean updateUserData(ProfileUserDto profileUserDto) {
        User user = getCurrentUser();
        if (user == null) {
            throw new UsernameNotFoundException(EX_MSG_USER_NOT_FOUND);
        }
        boolean exitFlag = false;
        List<UserContact> userContactList = new ArrayList<>(user.getUserContacts());
        for (UserContact prevContact : userContactList) {
            if (prevContact.getType().equals(ContactType.EMAIL) &&
                    !prevContact.getContact().equals(profileUserDto.getMail())) {
                user.removeContact(prevContact);
                userContactRepository.delete(prevContact);
                setUserInContact(profileUserDto.getMail(), user);
                exitFlag = true;
            }
            if (prevContact.getType().equals(ContactType.PHONE) &&
                    !prevContact.getContact().equals(clearPhoneNumber(profileUserDto.getPhone()))) {
                user.removeContact(prevContact);
                userContactRepository.delete(prevContact);
                setUserInContact(clearPhoneNumber(profileUserDto.getPhone()), user);
                exitFlag = true;
            }
        }
        return exitFlag;
    }

    /**
     * Метод проверяет, код подтверждения контакта. Код подтверждения может быть просрочен, может быть исчерпано
     * количество попыток его ввода, а также код может быть введён неверно: в этом случае, возвращается ошибка.
     * В первых двух случаях также должен отправлять параметр “return”, равный “true”, который означает, что
     * необходимо предоставить пользователю возможность запросить новый код. В противном случае контакт активируется
     * (user_contact.approved становится равным 1), а метод возвращает значение “result”, равное “true”.
     *
     * @param contact - строка Email или телефон
     * @param code    - код подтверждения
     * @return Integer - id пользователя, к которому привязан контакт (null если контакт не привязан)
     * @throws CheckCodeException - сообщение на установленном языке об ошибке (отправляется пользователю)
     */
    public Integer approveContact(String contact, String code) throws CheckCodeException {
        Contact curContact = identifyContact(contact);
        Optional<UserContact> userContact = userContactRepository.findByContact(curContact.contactValue);
        if (!userContact.isPresent()) {
            throw new CheckCodeException(new ApproveContactResponse(LanguageMessage.getExMsgContactNotFound(), false));
        }
        incrementCodeTrials(userContact.get());
        checkCodeContact(userContact.get(), code);
        setApproveCode(userContact.get());
        return userContact.get().getUser().getId();
    }

    /**
     * Метод проверяет корректность переданного контакта
     * Если контакт отсутствует в базе данных или контакт не подтвержден выбрасывается исключение SendCodeException
     * Если запрос кода подтверждения повторный, то, в случае если срок действия кода подтверждения не истёк и
     * количество попыток его ввода не исчерпано:
     * ●	Если превышен таймаут повторной отправки кода, на переданный контакт отправляется текущий код подтверждения.
     * ●	Если таймаут повторной отправки кода не превышен, выбрасывается исключение SendCodeException.
     * Если исчерпано количество попыток ввода кода, то проверяется, прошло ли достаточно времени с момента генерации
     * кода подтверждения. Если прошло достаточно, генерируется новый код. Если же нет — выбрасывается исключение
     * SendCodeException.
     * Если же истёк срок действия кода подтверждения, а количество попыток ввода не превышает лимит, генерируется и
     * отправляется новый код.
     *
     * @param contact - строка Email или телефон
     * @throws SendCodeException - сообщение на установленном языке об ошибке (отправляется пользователю)
     */
    public void loginContactConfirmation(String contact) throws SendCodeException, NoSuchAlgorithmException,
            JsonProcessingException, SendSMSException {
        Contact curContact = identifyContact(contact);
        Optional<UserContact> userContact = userContactRepository.findByContact(curContact.contactValue);
        if (!userContact.isPresent() || userContact.get().getApproved() == BookShopConfig.NOT_APPROVE_CONTACT ||
                userContact.get().getUser().getId() == BookShopConfig.SERVICE_USER_ID) {
            throw new SendCodeException(LanguageMessage.getExMsgContactNotFound());
        }
        sendCodeByContact(userContact.get());
    }

    /**
     * Метод проверяет корректность переданного контакта
     * Если контакт отсутствует в базе данных, то создается новый и по нему отправляется код.
     * Если контакт присутствует в базе данных и он одобрен, то выбрасывается исключение SendCodeException
     * Если запрос кода подтверждения повторный, то, в случае если срок действия кода подтверждения не истёк и
     * количество попыток его ввода не исчерпано:
     * ●	Если превышен таймаут повторной отправки кода, на переданный контакт отправляется текущий код подтверждения.
     * ●	Если таймаут повторной отправки кода не превышен, выбрасывается исключение SendCodeException.
     * Если исчерпано количество попыток ввода кода, то проверяется, прошло ли достаточно времени с момента генерации
     * кода подтверждения. Если прошло достаточно, генерируется новый код. Если же нет — выбрасывается исключение
     * SendCodeException.
     * Если же истёк срок действия кода подтверждения, а количество попыток ввода не превышает лимит, генерируется и
     * отправляется новый код.
     *
     * @param contact - строка Email или телефон
     * @throws SendCodeException - сообщение на установленном языке об ошибке (отправляется пользователю)
     */
    public void registerContactConfirmation(String contact) throws SendCodeException, NoSuchAlgorithmException,
            JsonProcessingException, NotFoundException, SendSMSException {
        Contact curContact = identifyContact(contact);
        Optional<UserContact> userContact = userContactRepository.findByContact(curContact.contactValue);
        if (userContact.isPresent()) {
            if (userContact.get().getApproved() == BookShopConfig.APPROVE_CONTACT &&
                    userContact.get().getUser().getId() != BookShopConfig.SERVICE_USER_ID) {
                throw new SendCodeException(LanguageMessage.getExMsgContactIsRegistered());
            }
            sendCodeByContact(userContact.get());
            return;
        }
        String code = generateCode();
        saveNewContact(curContact.contactValue, curContact.contactType, code);
        sendCode(curContact.contactValue, curContact.contactType, code);
    }

    private Contact identifyContact(String contact) {
        Contact curContact = new Contact();
        if (isEmail(contact)) {
            curContact.contactValue = contact;
            curContact.contactType = ContactType.EMAIL;
            return curContact;
        }
        curContact.contactValue = clearPhoneNumber(contact);
        if (isPhone(curContact.contactValue)) {
            curContact.contactType = ContactType.PHONE;
            return curContact;
        }
        throw new NoSuchParameterException("identifyContact - contact not identified");
    }

    private static class Contact {
        String contactValue;
        ContactType contactType;
    }

    private void checkCodeContact(UserContact contact, String code) throws CheckCodeException {
        if (contact.getCodeTrials() > config.getCodeMaxTrialsEntry()) {
            throw new CheckCodeException(
                    new ApproveContactResponse(LanguageMessage.getExMsgCodeIsExceededCountTrialsValue(), true));
        }
        if (isExpiredCode(contact)) {
            throw new CheckCodeException(new ApproveContactResponse(LanguageMessage.getExMsgCodeIsExpired(), true));
        }
        if (!contact.getCode().equals(code)) {
            throw new CheckCodeException(new ApproveContactResponse(LanguageMessage.getExMsgCodeIsWrong(), false));
        }
    }

    private void sendCodeByContact(UserContact contact) throws NoSuchAlgorithmException,
            JsonProcessingException, SendCodeException, SendSMSException {
        if (contact == null) {
            log.error("sendCodeByContact - contact is Null");
            throw new NoSuchParameterException("sendCodeByContact - contact is Null");
        }
        if (!isExpiredCode(contact) && contact.getCodeTrials() <= config.getCodeMaxTrialsEntry()) {
            long timeout = countMinutesToExpireTimeoutCode(contact);
            if (timeout == 0) {
                sendCode(contact.getContact(), contact.getType(), contact.getCode());
                return;
            } else {
                throw new SendCodeException(LanguageMessage.getExMsgCodeTimeout(timeout));
            }
        }
        if (!isExpiredCode(contact) && contact.getCodeTrials() > config.getCodeMaxTrialsEntry()) {
            throw new SendCodeException(LanguageMessage.getExMsgCodeTrials(countMinutesToExpireCode(contact)));
        }
        String code = generateCode();
        saveNewCode(contact, code);
        sendCode(contact.getContact(), contact.getType(), code);
    }

    private void sendCode(String contact, ContactType type, String code) throws
            JsonProcessingException, NoSuchAlgorithmException, SendSMSException {
        switch (type) {
            case PHONE:
                smsService.sendSMS(contact, code);
                break;
            case EMAIL:
                mailService.sendMailCode(contact, code);
                break;
            default:
                log.error("sendCode - ContactType - Invalid parameter value.");
                throw new NoSuchParameterException("sendCode - ContactType - Invalid parameter value.");
        }
    }

    private long countMinutesToExpireTimeoutCode(UserContact contact) {
        if (contact.getCodeTime() != null) {
            LocalDateTime timeout = contact.getCodeTime().plusMinutes(config.getCodeTimeOut());
            if (LocalDateTime.now().isAfter(timeout)) {
                return 0;
            } else {
                return Duration.between(LocalDateTime.now(), timeout).toMinutes();
            }
        }
        return 0;
    }

    private long countMinutesToExpireCode(UserContact contact) {
        if (contact.getCodeTime() != null) {
            LocalDateTime expireTime = contact.getCodeTime().plusMinutes(config.getCodeExpiredTime());
            if (LocalDateTime.now().isAfter(expireTime)) {
                return 0;
            } else {
                return Duration.between(LocalDateTime.now(), expireTime).toMinutes();
            }
        }
        return 0;
    }

    private void incrementCodeTrials(UserContact contact) {
        int currentCodeTrials = contact.getCodeTrials();
        contact.setCodeTrials(++currentCodeTrials);
        userContactRepository.save(contact);
    }

    private void setApproveCode(UserContact contact) {
        contact.setApproved(BookShopConfig.APPROVE_CONTACT);
        contact.setCode(null);
        contact.setCodeTime(null);
        contact.setCodeTrials(0);
        userContactRepository.save(contact);
    }

    private void setUserInContact(String contact, User user) {
        Contact curContact = identifyContact(contact);
        Optional<UserContact> userContact = userContactRepository.findByContact(curContact.contactValue);
        if (!userContact.isPresent()) {
            throw new NoSuchElementException("setUserIdInContact - contact no found in data base");
        }
        userContact.get().setUser(user);
        userContactRepository.save(userContact.get());
    }

    protected User saveNewUser(String name) {
        User user = new User();
        user.setName(name);
        user.setBalance(0);
        user.setHash(generateHashCode());
        user.setRegTime(LocalDateTime.now());
        return userRepository.save(user);
    }

    private void saveNewCode(UserContact contact, String code) {
        contact.setCode(code);
        contact.setCodeTrials(0);
        contact.setCodeTime(LocalDateTime.now());
        userContactRepository.save(contact);
    }

    public UserContact saveNewContact(String contact, ContactType type, String code) throws NotFoundException {
        UserContact userContact = new UserContact();
        userContact.setContact(contact);
        userContact.setType(type);
        userContact.setApproved(BookShopConfig.NOT_APPROVE_CONTACT);
        userContact.setCode(code);
        userContact.setCodeTrials(0);
        userContact.setCodeTime(LocalDateTime.now());
        userContact.setUser(getServiceUser());
        userContact = userContactRepository.save(userContact);
        userContactAddedList.add(userContact);
        return userContact;
    }

    /**
     * Для данного метода зарезервирован id = 1.
     * Метод обеспечивает получение сервисного пользователя, необходимого для временной
     * привязки создаваемых контактов при регистрации. Если контакт ссылается на id данного
     * пользователя, то по нему невозможно выполнить вход.
     */
    private User getServiceUser() throws NotFoundException {
        Optional<User> user = userRepository.findById(BookShopConfig.SERVICE_USER_ID);
        if (!user.isPresent()) {
            throw new NotFoundException("getServiceUser - service user not found at database");
        }
        return user.get();
    }

    public String generateCode() {

        StringBuilder sb = new StringBuilder();
        while (sb.length() < 6) {
            sb.append(random.nextInt(9));
        }
        sb.insert(3, " ");
        return sb.toString();
    }

    private boolean isPhone(String phone) {
        Pattern pattern = Pattern.compile(PHONE_REGEX);
        Matcher matcher = pattern.matcher(phone);
        return matcher.find();
    }

    private boolean isEmail(String email) {
        Pattern pattern = Pattern.compile(EMAIL_REGEX);
        Matcher matcher = pattern.matcher(email);
        return matcher.find();
    }

    private String clearPhoneNumber(String phone) {
        return phone.replaceAll("[+( )-]", "");
    }

    private boolean isExpiredCode(UserContact contact) {
        if (contact.getCodeTime() == null) {
            return true;
        }
        return LocalDateTime.now().isAfter(contact.getCodeTime().plusMinutes(config.getCodeExpiredTime()));
    }

    protected String generateHashCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < USER_HASH_LENGTH; i++) {
            sb.append(SYMBOLS[random.nextInt(SYMBOLS.length)]);
        }
        return sb.toString();
    }
}
