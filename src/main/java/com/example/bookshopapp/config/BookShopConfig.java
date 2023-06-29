package com.example.bookshopapp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(value = "app-config")
public class BookShopConfig {
    public static final String LOGIN_URI = "/signin";
    public static final String LOGOUT_URI = "/logout";
    public static final String SORT_PARAM_RECOMMENDED = "rating";
    public static final String SORT_PARAM_PUBLICATION_DATE = "pubDate";
    public static final String SORT_PARAM_POPULAR_INDEX = "popularIndex";
    public static final String SORT_PARAM_TRANSACTION = "time";

    public static final String BOOK_REVIEW_HASH_COOKIE_NAME = "rateBookReviewHash";
    public static final String CART_COOKIE_NAME = "cartContents";
    public static final String POSTPONED_COOKIE_NAME = "postponedContents";
    public static final String BOOK_RATE_COOKIE_NAME = "bookRateContents";
    public static final String TOKEN_COOKIE_NAME = "token";

    public static final String TAG_WEIGHT_0_CLASS_NAME = "Tag Tag_xs";
    public static final String TAG_WEIGHT_1_CLASS_NAME = "Tag Tag_sm";
    public static final String TAG_WEIGHT_2_CLASS_NAME = "Tag";
    public static final String TAG_WEIGHT_3_CLASS_NAME = "Tag Tag_md";
    public static final String TAG_WEIGHT_4_CLASS_NAME = "Tag Tag_lg";
    public static final Double TAG_WEIGHT_0_MAX_VALUE = 0.2;
    public static final Double TAG_WEIGHT_1_MAX_VALUE = 0.4;
    public static final Double TAG_WEIGHT_2_MAX_VALUE = 0.6;
    public static final Double TAG_WEIGHT_3_MAX_VALUE = 0.9;
    public static final Double TAG_WEIGHT_4_MAX_VALUE = 1.0;

    public static final String DATE_FORMAT_API = "dd.MM.yyyy";
    public static final String DATE_TIME_FORMAT_VIEW = "dd.MM.yyyy hh:mm";
    public static final String BOOK_UPDATE_FREQUENCY = "0 0 4 * * *"; //Every day at 4 am (Every 10 sec: "*/10 * * * * *")
    public static final String TOKEN_CLEAR_BLACK_LIST_FREQUENCY = "0 0 * * * *"; //Every 1 hour
    public static final double RATIO_BYTES_TO_MB = 0.00000095367432;
    public static final String RATIO_BYTES_TO_MB_TEXT = "Mb";
    public static final long DAYS_EVALUATION_VIEWS = 10;

    public static final int REVIEW_MIN_SHORT_LENGTH = 400;
    public static final int REVIEW_MAX_SHORT_LENGTH = 700;
    public static final byte REVIEW_LIKE = 1;
    public static final byte REVIEW_DISLIKE = -1;
    public static final byte APPROVE_CONTACT = 1;
    public static final byte NOT_APPROVE_CONTACT = 0;
    public static final int SERVICE_USER_ID = 1;

    public static final long TOKEN_EXPIRATION = 1000 * 60 * 60 * 10L;

    public static final String PHONE_REGEX = "7\\d{10}";
    public static final String EMAIL_REGEX = "\\w+([.-]?\\w+)*@\\w+([.-]?\\w+)*\\.\\w{2,4}";
    public static final char[] SYMBOLS = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O',
            'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
            'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4',
            '5', '6', '7', '8', '9'};
    public static final int USER_HASH_LENGTH = 10;
    public static final int LENGTH_IDEMPOTENCE_KEY = 20;
    public static final String IDEMPOTENCE_KEY_NAME = "Idempotence-Key";
    public static final int TIME_CACHING_PAYMENT_REQUEST_SEC = 30;
    public static final int DELAY_PAYMENT_CONFIRMATION = 2000;

    private String shopName;
    private Integer thPageBookShowLimit;

    private String uploadPath;
    private String downloadPath;
    private Integer maxDownloadCount;

    private String authSecret;

    private String smsApiHost;
    private String smsPublicKey;
    private String smsPrivateKey;
    private String smsSenderName;
    private String smsCodeText;

    private String emailAdr;
    private String emailPass;
    private String emailSMTP;
    private Integer emailPort;
    private String emailCodeSubject;
    private String emailCodeText;

    private Integer codeMaxTrialsEntry;
    private Integer codeExpiredTime;
    private Integer codeTimeOut;

    private String oauthRedirectURI;

    private String paymentSecret;
    private String paymentId;
    private String paymentUri;
    private String paymentCurrency;
    private String paymentMethodData;
    private String paymentRedirectUriBalance;
    private String paymentRedirectUriCart;
}