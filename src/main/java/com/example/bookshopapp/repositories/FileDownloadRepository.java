package com.example.bookshopapp.repositories;

import com.example.bookshopapp.model.Book;
import com.example.bookshopapp.model.FileDownload;
import com.example.bookshopapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FileDownloadRepository extends JpaRepository<FileDownload, Integer> {
    Optional<FileDownload> getByBookAndUser(Book book, User user);
}
