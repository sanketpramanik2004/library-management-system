package com.library.library_management.controller;

import com.library.library_management.dto.DashboardStatsDTO;
import com.library.library_management.entity.Transaction;
import com.library.library_management.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    // ISSUE
    @PostMapping("/issue/{bookId}")
    public String issueBook(@PathVariable Long bookId,
            Authentication authentication) {

        String username = authentication.getName();

        return transactionService.issueBook(bookId, username);
    }

    // RETURN
    @PostMapping("/return/{transactionId}")
    public String returnBook(@PathVariable Long transactionId) {

        return transactionService.returnBook(transactionId);
    }

    @GetMapping("/my-books")
    public List<Transaction> myBooks(Authentication authentication) {

        String username = authentication.getName();

        return transactionService.getIssuedBooks(username);
    }

    @GetMapping("/all")
    public List<Transaction> getAllTransactions() {
        return transactionService.getAllTransactions();
    }

    @GetMapping("/stats")
    public DashboardStatsDTO getStats() {
        return transactionService.getDashboardStats();
    }

    @GetMapping("/history")
    public List<Transaction> getHistory(Authentication authentication) {

        String username = authentication.getName();

        return transactionService.getUserTransactions(username);
    }

}
