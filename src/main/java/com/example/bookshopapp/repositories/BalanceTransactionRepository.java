package com.example.bookshopapp.repositories;

import com.example.bookshopapp.model.BalanceTransaction;
import com.example.bookshopapp.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BalanceTransactionRepository extends JpaRepository<BalanceTransaction, Integer> {
    Page<BalanceTransaction> findAllByUser(User user, Pageable pageable);
}
