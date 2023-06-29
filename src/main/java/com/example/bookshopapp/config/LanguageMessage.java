package com.example.bookshopapp.config;

import org.springframework.context.i18n.LocaleContextHolder;

public class LanguageMessage {
    public static final String EX_MSG_PAYMENT_SERVICE_ERROR = "Payment system error.";
    public static final String EX_MSG_RESOURCE_NOT_FOUND = "Resource not found";
    public static final String EX_MSG_USER_NOT_FOUND = "User not found!";
    public static final String EX_MSG_WRONG_PARAM = "Wrong values passed to one or more parameters";
    public static final String EX_MSG_EMPTY_PARAM = "An empty parameter in the query string";
    public static final String EX_MSG_MISSING_RESULT = "The result of the query from the database is missing. " +
            "Invalid request parameter.";
    public static final String VIEW_STRING_AND_OTHER_EN = " and other";
    public static final String VIEW_STRING_AND_OTHER_RU = " и другие";
    public static final String EX_MSG_CODE_TIMEOUT_RU = "Повторно запросить код можно через ";
    public static final String EX_MSG_CODE_TIMEOUT_EN = "You can request the code again in ";
    public static final String EX_MSG_CODE_TRIALS_RU = "Исчерпано количество попыток ввода кода подтверждения. " +
            "Запросите новый код через ";
    public static final String EX_MSG_CODE_TRIALS_EN = "The number of attempts to enter the confirmation code has " +
            "been exhausted. Request a new code in ";
    public static final String EX_MSG_CONTACT_NOT_FOUND_RU = "Контакт введен не корректно или не найден";
    public static final String EX_MSG_CONTACT_NOT_FOUND_EN = "Contact entered incorrectly or not found";
    public static final String EX_MSG_CONTACT_IS_REGISTERED_RU = "Контакт уже зарегистрирован";
    public static final String EX_MSG_CONTACT_IS_REGISTERED_EN = "The contact has already been registered";
    public static final String EX_MSG_CODE_IS_WRONG_RU = "Код подтверждения введен неверно";
    public static final String EX_MSG_CODE_IS_WRONG_EN = "The confirmation code is entered incorrectly";
    public static final String EX_MSG_CODE_IS_EXPIRED_RU = "Код подтверждения устарел. Запросите новый";
    public static final String EX_MSG_CODE_IS_EXPIRED_EN = "The confirmation code is outdated. Request a new one";
    public static final String EX_MSG_CODE_IS_EXCEEDED_COUNT_TRIALS_VALUE_RU = "Превышено количество попыток ввода " +
            "кода. Попробуйте запросить код позже";
    public static final String EX_MSG_CODE_IS_EXCEEDED_COUNT_TRIALS_VALUE_EN = "Exceeded the number of attempts to " +
            "enter the code. Try requesting the code later";
    public static final String EX_MSG_OPERATION_FAILED_RU = "Операция не выполнена. повторите попытку позже";
    public static final String EX_MSG_OPERATION_FAILED_EN = "Operation failed. Try again later";

    public static final String MSG_RECEIPT_DESCRIPTION_RU = "Пополнение баланса пользователя ";
    public static final String MSG_RECEIPT_DESCRIPTION_EN = "Replenishment of the user's balance ";

    public static final String MSG_PAID_BOOK_DESCRIPTION_RU = "Покупка книги: ";
    public static final String MSG_PAID_BOOK_DESCRIPTION_EN = "Buying a book: ";

    private LanguageMessage() {
    }

    public static String getAuthorsName(String firstName) {
        if (LocaleContextHolder.getLocale().getLanguage().equals("ru")) {
            return firstName + VIEW_STRING_AND_OTHER_RU;
        }
        return firstName + VIEW_STRING_AND_OTHER_EN;
    }

    public static String getExMsgCodeTimeout(long value) {
        if (LocaleContextHolder.getLocale().getLanguage().equals("ru")) {
            return EX_MSG_CODE_TIMEOUT_RU + value + " мин.";
        }
        return EX_MSG_CODE_TIMEOUT_EN + value + " min.";
    }

    public static String getExMsgCodeTrials(long value) {
        if (LocaleContextHolder.getLocale().getLanguage().equals("ru")) {
            return EX_MSG_CODE_TRIALS_RU + value + " мин.";
        }
        return EX_MSG_CODE_TRIALS_EN + value + " min.";
    }

    public static String getExMsgContactNotFound() {
        if (LocaleContextHolder.getLocale().getLanguage().equals("ru")) {
            return EX_MSG_CONTACT_NOT_FOUND_RU;
        }
        return EX_MSG_CONTACT_NOT_FOUND_EN;
    }

    public static String getExMsgContactIsRegistered() {
        if (LocaleContextHolder.getLocale().getLanguage().equals("ru")) {
            return EX_MSG_CONTACT_IS_REGISTERED_RU;
        }
        return EX_MSG_CONTACT_IS_REGISTERED_EN;
    }

    public static String getExMsgCodeIsWrong() {
        if (LocaleContextHolder.getLocale().getLanguage().equals("ru")) {
            return EX_MSG_CODE_IS_WRONG_RU;
        }
        return EX_MSG_CODE_IS_WRONG_EN;
    }

    public static String getExMsgCodeIsExpired() {
        if (LocaleContextHolder.getLocale().getLanguage().equals("ru")) {
            return EX_MSG_CODE_IS_EXPIRED_RU;
        }
        return EX_MSG_CODE_IS_EXPIRED_EN;
    }

    public static String getExMsgCodeIsExceededCountTrialsValue() {
        if (LocaleContextHolder.getLocale().getLanguage().equals("ru")) {
            return EX_MSG_CODE_IS_EXCEEDED_COUNT_TRIALS_VALUE_RU;
        }
        return EX_MSG_CODE_IS_EXCEEDED_COUNT_TRIALS_VALUE_EN;
    }

    public static String getExMsgOperationFailed() {
        if (LocaleContextHolder.getLocale().getLanguage().equals("ru")) {
            return EX_MSG_OPERATION_FAILED_RU;
        }
        return EX_MSG_OPERATION_FAILED_EN;
    }

    public static String getMsgReceiptDescription(String userContact) {
        if (LocaleContextHolder.getLocale().getLanguage().equals("ru")) {
            return MSG_RECEIPT_DESCRIPTION_RU + userContact;
        }
        return MSG_RECEIPT_DESCRIPTION_EN + userContact;
    }

    public static String getMsgPaidBookDescription(String bookName, String bookSug) {
        if (LocaleContextHolder.getLocale().getLanguage().equals("ru")) {
            return MSG_PAID_BOOK_DESCRIPTION_RU + "&#32;<a href=\"/books/" + bookSug + "\">" + bookName + "</a>";
        }
        return MSG_PAID_BOOK_DESCRIPTION_EN + "&#32;<a href=\"/books/" + bookSug + "\">" + bookName + "</a>";
    }
}
