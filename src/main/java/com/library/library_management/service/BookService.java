package com.library.library_management.service;

import com.library.library_management.dto.BookDTO;
import com.library.library_management.entity.Book;
import com.library.library_management.entity.Transaction;
import com.library.library_management.exception.BookNotFoundException;
import com.library.library_management.repository.BookRepository;
import com.library.library_management.repository.TransactionRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final TransactionRepository transactionRepository;

    // ⭐ Constructor Injection (Industry Standard)
    public BookService(BookRepository bookRepository,
            TransactionRepository transactionRepository) {
        this.bookRepository = bookRepository;
        this.transactionRepository = transactionRepository;
    }

    // ✅ GET ALL BOOKS
    public List<BookDTO> getAllBooks() {

        return bookRepository.findAll()
                .stream()
                .map(book -> new BookDTO(
                        book.getId(),
                        book.getTitle(),
                        book.getAuthor()))
                .toList();
    }

    // ✅ ADD BOOK
    public Book addBook(Book book) {
        return bookRepository.save(book);
    }

    // ✅ ISSUE BOOK (Using Relationship)
    public String issueBook(Long bookId, String borrowerName) {

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + bookId));

        if (book.getAvailableCopies() <= 0) {
            throw new RuntimeException("No copies available for this book");
        }

        // Reduce available copies
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        // Create transaction
        Transaction transaction = new Transaction();
        transaction.setBook(book); // ⭐ Relationship instead of bookId
        transaction.setBorrowerName(borrowerName);
        transaction.setIssueDate(LocalDate.now());

        transactionRepository.save(transaction);

        return "Book issued successfully!";
    }

    public void deleteBook(Long id) {

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        bookRepository.delete(book);
    }

    // ✅ RETURN BOOK + FINE CALCULATION
    public String returnBook(Long transactionId) {

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (transaction.getReturnDate() != null) {
            throw new RuntimeException("Book already returned");
        }

        LocalDate today = LocalDate.now();
        transaction.setReturnDate(today);

        long daysBetween = ChronoUnit.DAYS
                .between(transaction.getIssueDate(), today);

        // Fine rule: ₹2 per day after 7 days
        if (daysBetween > 7) {
            double fine = (daysBetween - 7) * 2;
            transaction.setFine(fine);
        } else {
            transaction.setFine(0);
        }

        transactionRepository.save(transaction);

        // ⭐ No need to fetch book again!
        Book book = transaction.getBook();

        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);

        return "Book returned successfully. Fine: ₹" + transaction.getFine();
    }
}
