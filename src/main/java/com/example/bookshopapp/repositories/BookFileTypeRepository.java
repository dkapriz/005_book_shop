package com.example.bookshopapp.repositories;

import com.example.bookshopapp.model.BookFileType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookFileTypeRepository extends JpaRepository<BookFileType, Integer> {
}
