package com.example.bookshopapp.service;

import com.example.bookshopapp.config.BookShopConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MailService {

    private final BookShopConfig config;
    private final JavaMailSender javaMailSender;

    public MailService(BookShopConfig config, JavaMailSender javaMailSender) {
        this.config = config;
        this.javaMailSender = javaMailSender;
    }

    public void sendMailCode(String email, String code){
        SimpleMailMessage massage = new SimpleMailMessage();
        massage.setFrom(config.getEmailAdr());
        massage.setTo(email);
        massage.setSubject(config.getEmailCodeSubject());
        massage.setText(config.getEmailCodeText() + " " + code);
        javaMailSender.send(massage);
        log.info("Send mail " + massage + " by address " + email);
    }
}