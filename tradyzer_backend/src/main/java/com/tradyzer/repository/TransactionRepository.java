package com.tradyzer.repository;

import com.tradyzer.entity.Portfolio;
import com.tradyzer.entity.Transaction;
import com.tradyzer.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByPortfolioOrderByTransactionDateDesc(Portfolio portfolio);

    List<Transaction> findByUserOrderByTransactionDateDesc(User user);

    @Query("SELECT t FROM Transaction t WHERE t.portfolio = :portfolio AND t.transactionDate BETWEEN :startDate AND :endDate ORDER BY t.transactionDate DESC")
    List<Transaction> findByPortfolioAndDateRange(Portfolio portfolio, LocalDateTime startDate, LocalDateTime endDate);
}