package com.example.bookshopapp.repositories;

import com.example.bookshopapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    @Query(value = "SELECT u FROM User u " +
            "LEFT JOIN u.userContacts c WHERE c.type = 'PHONE' " +
            "AND c.approved = 1 AND c.contact = :phone")
    Optional<User> findUserByApprovedPhoneNumber(@Param("phone") String phone);

    @Query(value = "SELECT u FROM User u " +
            "LEFT JOIN u.userContacts c WHERE c.type = 'EMAIL' " +
            "AND c.approved = 1 AND c.contact = :email")
    Optional<User> findUserByApprovedEmail(@Param("email") String email);
}
