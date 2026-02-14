package com.library.library_management.service;

import com.library.library_management.entity.Book;
import com.library.library_management.entity.Transaction;
import com.library.library_management.entity.User;
import com.library.library_management.repository.BookRepository;
import com.library.library_management.repository.TransactionRepository;
import com.library.library_management.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.library.library_management.dto.DashboardStatsDTO;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.List;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    // ⭐ ISSUE BOOK
    public String issueBook(Long bookId, String username) {

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        if (book.getAvailableCopies() <= 0) {
            throw new RuntimeException("No copies available");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 🔥 Prevent duplicate issue
        Optional<Transaction> existing = transactionRepository.findByBookIdAndUserIdAndStatus(
                bookId,
                user.getId(),
                "ISSUED");

        if (existing.isPresent()) {
            throw new RuntimeException("You already issued this book!");
        }

        // Reduce book copies
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        Transaction transaction = new Transaction();
        transaction.setBook(book);
        transaction.setUser(user);
        transaction.setIssueDate(LocalDate.now());
        transaction.setDueDate(LocalDate.now().plusDays(7)); // ⭐ 7 day rule
        transaction.setStatus("ISSUED");
        transaction.setFine(0);

        transactionRepository.save(transaction);

        return "Book issued successfully!";
    }

    // ⭐ RETURN BOOK + FINE CALCULATION
    public String returnBook(Long transactionId) {

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (transaction.getStatus().equals("RETURNED")) {
            throw new RuntimeException("Book already returned");
        }

        LocalDate today = LocalDate.now();
        transaction.setReturnDate(today);

        // 🔥 Fine Logic
        if (today.isAfter(transaction.getDueDate())) {

            long daysLate = ChronoUnit.DAYS.between(
                    transaction.getDueDate(),
                    today);

            double fine = daysLate * 5; // ₹5 per day
            transaction.setFine(fine);
        }

        transaction.setStatus("RETURNED");

        // Increase available copies again
        Book book = transaction.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);

        transactionRepository.save(transaction);

        return "Book returned successfully!";
    }

    public List<Transaction> getUserTransactions(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return transactionRepository.findByUser(user);
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public DashboardStatsDTO getDashboardStats() {

        DashboardStatsDTO stats = new DashboardStatsDTO();

        stats.setTotalBooks(bookRepository.count());

        stats.setIssuedBooks(
                transactionRepository.countByStatus("ISSUED"));

        stats.setReturnedBooks(
                transactionRepository.countByStatus("RETURNED"));

        stats.setTotalFine(
                transactionRepository.sumTotalFine());

        return stats;
    }

}
