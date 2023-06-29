package com.example.bookshopapp.repositories;

import com.example.bookshopapp.model.User;
import com.example.bookshopapp.model.UserContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserContactRepository extends JpaRepository<UserContact, Integer> {
    Optional<UserContact> findByContact(String contact);
}