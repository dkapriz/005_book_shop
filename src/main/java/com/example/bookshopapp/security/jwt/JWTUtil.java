package com.example.bookshopapp.security.jwt;

import com.example.bookshopapp.config.BookShopConfig;
import com.example.bookshopapp.model.JWTBlackList;
import com.example.bookshopapp.repositories.JWTBlackListRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@Slf4j
public class JWTUtil {
    private final BookShopConfig config;
    private final JWTBlackListRepository jwtBlackListRepository;

    @Autowired
    public JWTUtil(BookShopConfig config, JWTBlackListRepository jwtBlackListRepository) {
        this.config = config;
        this.jwtBlackListRepository = jwtBlackListRepository;
    }

    private String createToken(Map<String, Object> claims, String username) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + BookShopConfig.TOKEN_EXPIRATION))
                .signWith(SignatureAlgorithm.HS256, config.getAuthSecret()).compact();
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    public String generateToken(DefaultOidcUser oidcUser) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, oidcUser.getEmail());
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(config.getAuthSecret()).parseClaimsJws(token).getBody();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token) &&
                !jwtBlackListRepository.existsByToken(token));
    }

    public void addToBlackList(String token) {
        JWTBlackList jwtBlackList = new JWTBlackList();
        jwtBlackList.setToken(token);
        jwtBlackList.setCreation(new Date());
        jwtBlackList.setExpiration(extractExpiration(token));
        jwtBlackListRepository.save(jwtBlackList);
    }

    @Scheduled(cron = BookShopConfig.TOKEN_CLEAR_BLACK_LIST_FREQUENCY)
    @Transactional
    public void clear() {
        jwtBlackListRepository.deleteAllByExpirationBefore(new Date());
        log.info("Clear JWT black list");
    }
}