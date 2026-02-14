package com.library.library_management.repository;

import com.library.library_management.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BookRepository
        extends JpaRepository<Book, Long>,
        JpaSpecificationExecutor<Book> {
}
