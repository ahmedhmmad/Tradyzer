package com.tradyzer.controller;

import com.tradyzer.dto.*;
import com.tradyzer.entity.Portfolio;
import com.tradyzer.entity.Transaction;
import com.tradyzer.service.PortfolioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/portfolios")
public class PortfolioController {

    @Autowired
    private PortfolioService portfolioService;

    // إنشاء محفظة جديدة
    @PostMapping
    public ResponseEntity<Portfolio> createPortfolio(
            Authentication auth,
            @Valid @RequestBody CreatePortfolioRequest request) {
        Portfolio portfolio = portfolioService.createPortfolio(auth.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(portfolio);
    }

    // الحصول على محافظ المستخدم
    @GetMapping
    public ResponseEntity<List<Portfolio>> getUserPortfolios(Authentication auth) {
        List<Portfolio> portfolios = portfolioService.getUserPortfolios(auth.getName());
        return ResponseEntity.ok(portfolios);
    }

    // الحصول على تفاصيل محفظة
    @GetMapping("/{id}")
    public ResponseEntity<PortfolioDTO> getPortfolioDetails(
            Authentication auth,
            @PathVariable Long id) {
        PortfolioDTO portfolio = portfolioService.getPortfolioDetails(id, auth.getName());
        return ResponseEntity.ok(portfolio);
    }

    // إجراء معاملة
    @PostMapping("/transactions")
    public ResponseEntity<Transaction> executeTransaction(
            Authentication auth,
            @Valid @RequestBody TransactionRequest request) {
        Transaction transaction = portfolioService.executeTransaction(auth.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }
}