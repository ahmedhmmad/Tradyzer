package com.tradyzer.service;

import com.tradyzer.dto.CreatePortfolioRequest;
import com.tradyzer.dto.PortfolioDTO;
import com.tradyzer.dto.TransactionRequest;
import com.tradyzer.entity.*;
import com.tradyzer.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class PortfolioService {

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private PortfolioHoldingRepository holdingRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BinanceService binanceService;

    // إنشاء محفظة جديدة
    public Portfolio createPortfolio(String username, CreatePortfolioRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (portfolioRepository.existsByUserAndName(user, request.getName())) {
            throw new RuntimeException("Portfolio with this name already exists");
        }

        Portfolio portfolio = new Portfolio();
        portfolio.setUser(user);
        portfolio.setName(request.getName());
        portfolio.setDescription(request.getDescription());
        portfolio.setInitialBalance(request.getInitialBalance());
        portfolio.setCurrentBalance(request.getInitialBalance());

        // إذا كانت أول محفظة، اجعلها افتراضية
        if (portfolioRepository.findByUserAndIsActiveTrue(user).isEmpty()) {
            portfolio.setDefault(true);
        }

        return portfolioRepository.save(portfolio);
    }

    // الحصول على محافظ المستخدم
    public List<Portfolio> getUserPortfolios(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return portfolioRepository.findByUserAndIsActiveTrue(user);
    }

    // الحصول على تفاصيل محفظة
    public PortfolioDTO getPortfolioDetails(Long portfolioId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Portfolio portfolio = portfolioRepository.findByIdWithHoldings(portfolioId)
                .orElseThrow(() -> new RuntimeException("Portfolio not found"));

        if (!portfolio.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to portfolio");
        }

        // تحديث أسعار العملات
        updateHoldingsPrices(portfolio);

        // حساب القيم
        calculatePortfolioValues(portfolio);

        return convertToDTO(portfolio);
    }

    @Transactional
    public Transaction executeTransaction(String username, TransactionRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // استخدام fetch للحصول على Portfolio مع Holdings
        Portfolio portfolio = portfolioRepository.findByIdWithHoldings(request.getPortfolioId())
                .orElseThrow(() -> new RuntimeException("Portfolio not found"));

        if (!portfolio.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to portfolio");
        }

        // إنشاء المعاملة
        Transaction transaction = new Transaction();
        transaction.setPortfolio(portfolio);
        transaction.setUser(user);
        transaction.setTransactionType(request.getTransactionType());
        transaction.setSymbol(request.getSymbol().toUpperCase());
        transaction.setQuantity(request.getQuantity());
        transaction.setPrice(request.getPrice());
        transaction.setTotal(request.getQuantity().multiply(request.getPrice()));
        transaction.setFee(request.getFee() != null ? request.getFee() : BigDecimal.ZERO);
        transaction.setNotes(request.getNotes());

        // حفظ المعاملة أولاً
        transaction = transactionRepository.save(transaction);

        // تحديث المحفظة
        updatePortfolioAfterTransaction(portfolio, transaction);

        // إرجاع المعاملة فقط (بدون Portfolio كامل)
        Transaction result = new Transaction();
        result.setId(transaction.getId());
        result.setTransactionType(transaction.getTransactionType());
        result.setSymbol(transaction.getSymbol());
        result.setQuantity(transaction.getQuantity());
        result.setPrice(transaction.getPrice());
        result.setTotal(transaction.getTotal());
        result.setFee(transaction.getFee());
        result.setNotes(transaction.getNotes());
        result.setTransactionDate(transaction.getTransactionDate());

        return result;
    }

    // تحديث المحفظة بعد المعاملة
    private void updatePortfolioAfterTransaction(Portfolio portfolio, Transaction transaction) {
        if (transaction.getTransactionType() == TransactionType.BUY) {
            // تحديث الرصيد
            BigDecimal totalCost = transaction.getTotal().add(transaction.getFee());
            portfolio.setCurrentBalance(portfolio.getCurrentBalance().subtract(totalCost));
            portfolio.setTotalInvested(portfolio.getTotalInvested().add(totalCost));

            // تحديث أو إنشاء holding
            PortfolioHolding holding = holdingRepository.findByPortfolioAndSymbol(portfolio, transaction.getSymbol())
                    .orElse(new PortfolioHolding());

            if (holding.getId() == null) {
                holding.setPortfolio(portfolio);
                holding.setSymbol(transaction.getSymbol());
                holding.setName(transaction.getSymbol()); // يمكن تحسينه لاحقاً
                holding.setQuantity(transaction.getQuantity());
                holding.setAverageBuyPrice(transaction.getPrice());
                holding.setTotalInvested(transaction.getTotal());
            } else {
                // حساب متوسط السعر الجديد
                BigDecimal totalQuantity = holding.getQuantity().add(transaction.getQuantity());
                BigDecimal totalValue = holding.getTotalInvested().add(transaction.getTotal());
                BigDecimal newAvgPrice = totalValue.divide(totalQuantity, 8, RoundingMode.HALF_UP);

                holding.setQuantity(totalQuantity);
                holding.setAverageBuyPrice(newAvgPrice);
                holding.setTotalInvested(totalValue);
            }

            holdingRepository.save(holding);

        } else if (transaction.getTransactionType() == TransactionType.SELL) {
            // تحديث الرصيد
            BigDecimal totalReceived = transaction.getTotal().subtract(transaction.getFee());
            portfolio.setCurrentBalance(portfolio.getCurrentBalance().add(totalReceived));

            // تحديث holding
            PortfolioHolding holding = holdingRepository.findByPortfolioAndSymbol(portfolio, transaction.getSymbol())
                    .orElseThrow(() -> new RuntimeException("No holding found for " + transaction.getSymbol()));

            BigDecimal newQuantity = holding.getQuantity().subtract(transaction.getQuantity());

            if (newQuantity.compareTo(BigDecimal.ZERO) <= 0) {
                // بيع كامل
                holdingRepository.delete(holding);
            } else {
                // بيع جزئي
                holding.setQuantity(newQuantity);
                BigDecimal newTotalInvested = holding.getAverageBuyPrice().multiply(newQuantity);
                holding.setTotalInvested(newTotalInvested);
                holdingRepository.save(holding);
            }
        }

        portfolioRepository.save(portfolio);
    }

    // تحديث أسعار العملات
    private void updateHoldingsPrices(Portfolio portfolio) {
        for (PortfolioHolding holding : portfolio.getHoldings()) {
            Map<String, Object> priceData = binanceService.getPrice(holding.getSymbol());
            if (!priceData.containsKey("error")) {
                BigDecimal currentPrice = (BigDecimal) priceData.get("price");
                holding.setCurrentPrice(currentPrice);
                holding.calculateValues();
                holdingRepository.save(holding);
            }
        }
    }

    // حساب قيم المحفظة
    private void calculatePortfolioValues(Portfolio portfolio) {
        BigDecimal totalValue = portfolio.getCurrentBalance();
        BigDecimal totalInvested = portfolio.getTotalInvested();

        for (PortfolioHolding holding : portfolio.getHoldings()) {
            if (holding.getCurrentValue() != null) {
                totalValue = totalValue.add(holding.getCurrentValue());
            }
        }

        portfolio.setTotalValue(totalValue);

        if (totalInvested.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal profitLoss = totalValue.subtract(totalInvested);
            portfolio.setProfitLoss(profitLoss);

            Double profitLossPercentage = profitLoss.divide(totalInvested, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100)).doubleValue();
            portfolio.setProfitLossPercentage(profitLossPercentage);
        }

        portfolioRepository.save(portfolio);
    }

    // تحويل إلى DTO
    private PortfolioDTO convertToDTO(Portfolio portfolio) {
        PortfolioDTO dto = new PortfolioDTO();
        dto.setId(portfolio.getId());
        dto.setName(portfolio.getName());
        dto.setDescription(portfolio.getDescription());
        dto.setInitialBalance(portfolio.getInitialBalance());
        dto.setCurrentBalance(portfolio.getCurrentBalance());
        dto.setTotalInvested(portfolio.getTotalInvested());
        dto.setTotalValue(portfolio.getTotalValue());
        dto.setProfitLoss(portfolio.getProfitLoss());
        dto.setProfitLossPercentage(portfolio.getProfitLossPercentage());
        dto.setHoldings(portfolio.getHoldings());
        dto.setCreatedAt(portfolio.getCreatedAt());
        dto.setUpdatedAt(portfolio.getUpdatedAt());

        return dto;
    }
}