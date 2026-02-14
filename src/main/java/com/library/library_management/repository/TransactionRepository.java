package com.library.library_management.repository;

import com.library.library_management.entity.Transaction;
import com.library.library_management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository
        extends JpaRepository<Transaction, Long> {

    // user's transactions
    List<Transaction> findByUser(User user);

    // active issued books
    List<Transaction> findByUserAndStatus(User user, String status);

    // prevent duplicate issue
    Optional<Transaction> findByBookIdAndUserIdAndStatus(
            Long bookId,
            Long userId,
            String status);

    // dashboard stats
    long countByStatus(String status);

    @Query("SELECT COALESCE(SUM(t.fine),0) FROM Transaction t")
    double sumTotalFine();

}
