package com.example.bookshopapp.repositories;

import com.example.bookshopapp.model.YooCashOperation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface YooCashOperationRepository extends JpaRepository<YooCashOperation, Integer> {
    Optional<YooCashOperation> findByOperationId(String operationId);
}