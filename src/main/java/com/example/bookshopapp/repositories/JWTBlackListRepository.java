package com.example.bookshopapp.repositories;

import com.example.bookshopapp.model.JWTBlackList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface JWTBlackListRepository extends JpaRepository<JWTBlackList, Integer> {
    void deleteAllByExpirationBefore(Date date);
    boolean existsByToken(String token);
}
