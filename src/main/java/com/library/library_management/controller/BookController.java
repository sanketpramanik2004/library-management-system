package com.library.library_management.controller;

import com.library.library_management.dto.BookDTO;
import com.library.library_management.entity.Book;
import com.library.library_management.service.BookService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/books")
@CrossOrigin
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    // GET ALL BOOKS
    @GetMapping
    public List<BookDTO> getAllBooks() {
        return bookService.getAllBooks();
    }

    // ADD BOOK
    @PostMapping("/add")
    public Book addBook(@RequestBody Book book) {
        return bookService.addBook(book);
    }

    // DELETE BOOK

    // SEARCH BOOKS
    @GetMapping("/search")
    public List<Book> searchBooks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String isbn) {

        return bookService.searchBooks(title, author, category, isbn);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteBook(@PathVariable Long id) {

        System.out.println("DELETE BOOK ID = " + id);

        bookService.deleteBook(id);

        return ResponseEntity.ok("Book deleted successfully!");
    }

}
