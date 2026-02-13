package com.library.library_management.repository;

import com.library.library_management.entity.Transaction;
import com.library.library_management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Find all issued books for a user
    List<Transaction> findByUserAndStatus(User user, String status);

    // Prevent issuing the same book twice before returning
    Optional<Transaction> findByBookIdAndUserIdAndStatus(
            Long bookId,
            Long userId,
            String status);
}
