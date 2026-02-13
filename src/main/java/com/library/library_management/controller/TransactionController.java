package com.library.library_management.controller;

import com.library.library_management.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
}
