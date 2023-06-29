package com.example.bookshopapp.service;

import com.example.bookshopapp.config.BookShopConfig;
import com.example.bookshopapp.config.LanguageMessage;
import com.example.bookshopapp.exception.SendSMSException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class SMSService {
    private final BookShopConfig config;
    private final WebClient webClient;

    @Autowired
    public SMSService(BookShopConfig config, WebClient webClient) {
        this.config = config;
        this.webClient = webClient;
    }

    public void sendCode(String phone, String code) throws NoSuchAlgorithmException, JsonProcessingException,
            SendSMSException {
        sendSMS(phone, config.getSmsCodeText() + " " + code);
    }

    public void sendSMS(String phone, String text) throws NoSuchAlgorithmException, JsonProcessingException,
            SendSMSException {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("key", config.getSmsPublicKey());
        params.add("sender", config.getSmsSenderName());
        params.add("text", text);
        params.add("phone", phone);
        params.add("datetime", "");
        params.add("lifetime", "0");
        params.add("sum", getSumMD5(params));
        String jsonResponse = webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/sendSMS")
                        .queryParams(params)
                        .build()
                )
                .retrieve()
                .bodyToMono(String.class)
                .block();
        log.info("Send SMS by phone number " + phone);
        getResultFromJSON(jsonResponse);
    }

    private void getResultFromJSON(String json) throws JsonProcessingException, SendSMSException {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> response = objectMapper.readValue(json, new TypeReference<Map<String, Object>>(){});
        if(response.containsKey("error")){
            log.warn("Error SMS send " + response.get("error"));
            throw new SendSMSException(LanguageMessage.getExMsgOperationFailed());
        }
        log.info("Successful SMS sending");
    }

    private String getSumMD5(MultiValueMap<String, String> map) throws NoSuchAlgorithmException {
        MultiValueMap<String, String> sortedMap = map;
        sortedMap.add("version", "3.0");
        sortedMap.add("action", "sendSMS");
        sortedMap = sortByKeys(sortedMap);
        StringBuilder sum = new StringBuilder();
        for(Map.Entry<String, List<String>> entry : sortedMap.entrySet()){
            if(entry.getValue().isEmpty()){
                throw new NoSuchAlgorithmException();
            }
            sum.append(entry.getValue().get(0));
        }
        sum.append(config.getSmsPrivateKey());
        return getMD5(sum.toString());
    }

    private String getMD5(String str) throws NoSuchAlgorithmException {
        byte[] bytesOfMessage = str.getBytes(StandardCharsets.UTF_8);
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(bytesOfMessage);
        BigInteger bigInt = new BigInteger(1, digest);
        StringBuilder md5Hex = new StringBuilder(bigInt.toString(16));

        while( md5Hex.length() < 32 ){
            md5Hex.insert(0, "0");
        }
        return md5Hex.toString();
    }

    private static <K extends Comparable<K>, V> MultiValueMap<K, V> sortByKeys(MultiValueMap<K, V> map)
    {
        List<K> keys = new ArrayList<>(map.keySet());
        Collections.sort(keys);

        MultiValueMap<K, V> linkedMultiValueMap = new LinkedMultiValueMap<>();
        for (K key: keys) {
            linkedMultiValueMap.put(key, map.get(key));
        }
        return linkedMultiValueMap;
    }
}
