package com.example.bookshopapp.repositories;

import com.example.bookshopapp.model.Book2UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface Book2UserTypeRepository extends JpaRepository<Book2UserType, Integer> {
    Optional<Book2UserType> getBook2UserTypeByCode(String code);
}
