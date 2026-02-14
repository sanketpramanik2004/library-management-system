package com.library.library_management.service;

import com.library.library_management.dto.BookDTO;
import com.library.library_management.entity.Book;
import com.library.library_management.repository.BookRepository;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {

    private final BookRepository bookRepository;

    // ✅ Constructor Injection
    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    // =====================================================
    // ✅ GET ALL BOOKS (DTO RESPONSE)
    // =====================================================
    public List<BookDTO> getAllBooks() {

        return bookRepository.findAll()
                .stream()
                .map(book -> new BookDTO(
                        book.getId(),
                        book.getTitle(),
                        book.getAuthor()))
                .toList();
    }

    // =====================================================
    // ✅ ADD BOOK (ADMIN)
    // =====================================================
    public Book addBook(Book book) {

        if (book.getAvailableCopies() <= 0) {
            book.setAvailableCopies(1);
        }

        return bookRepository.save(book);
    }

    // =====================================================
    // ✅ DELETE BOOK (ADMIN)
    // =====================================================
    public void deleteBook(Long id) {

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));

        bookRepository.delete(book);
    }

    // =====================================================
    // ✅ SEARCH BOOKS (ADVANCED FILTERS)
    // =====================================================
    public List<Book> searchBooks(
            String title,
            String author,
            String category,
            String isbn) {

        Specification<Book> spec = (root, query, cb) -> cb.conjunction();

        if (title != null && !title.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(
                    cb.lower(root.get("title")),
                    "%" + title.toLowerCase() + "%"));
        }

        if (author != null && !author.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(
                    cb.lower(root.get("author")),
                    "%" + author.toLowerCase() + "%"));
        }

        if (category != null && !category.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(
                    cb.lower(root.get("category")),
                    category.toLowerCase()));
        }

        if (isbn != null && !isbn.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("isbn"), isbn));
        }

        return bookRepository.findAll(spec);
    }
}
