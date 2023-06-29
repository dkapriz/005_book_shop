package com.example.bookshopapp.repositories;

import com.example.bookshopapp.model.BookFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookFileRepository extends JpaRepository<BookFile, Integer> {
    Optional<BookFile> findBookFileByHash(String hash);
}
