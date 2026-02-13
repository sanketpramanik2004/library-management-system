package com.library.library_management.controller;

import com.library.library_management.dto.BookDTO;
import com.library.library_management.entity.Book;
import com.library.library_management.service.BookService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/books")
@CrossOrigin
public class BookController {

    private final BookService bookService;

    // Constructor injection
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    // GET all books
    @GetMapping
    public List<BookDTO> getAllBooks() {
        return bookService.getAllBooks();
    }

    // ADD a book
    @PostMapping
    public Book addBook(@RequestBody Book book) {
        return bookService.addBook(book);
    }

    @GetMapping("/issue")
    public String issueBook(@RequestParam Long bookId,
            @RequestParam String borrowerName) {

        return bookService.issueBook(bookId, borrowerName);
    }

    @GetMapping("/return")
    public String returnBook(@RequestParam Long transactionId) {

        return bookService.returnBook(transactionId);
    }

}
