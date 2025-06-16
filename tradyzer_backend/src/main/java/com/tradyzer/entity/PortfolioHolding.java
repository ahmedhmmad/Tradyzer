package com.tradyzer.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "portfolio_holdings")
public class PortfolioHolding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false)
    private String name;

    @Column(precision = 20, scale = 8)
    private BigDecimal quantity;

    @Column(name = "average_buy_price", precision = 20, scale = 8)
    private BigDecimal averageBuyPrice;

    @Column(name = "current_price", precision = 20, scale = 8)
    private BigDecimal currentPrice;

    @Column(name = "total_invested", precision = 20, scale = 2)
    private BigDecimal totalInvested;

    @Column(name = "current_value", precision = 20, scale = 2)
    private BigDecimal currentValue;

    @Column(name = "profit_loss", precision = 20, scale = 2)
    private BigDecimal profitLoss;

    @Column(name = "profit_loss_percentage")
    private Double profitLossPercentage;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated = LocalDateTime.now();

    // Helper methods
    public void calculateValues() {
        if (quantity != null && currentPrice != null) {
            currentValue = quantity.multiply(currentPrice);

            if (totalInvested != null && totalInvested.compareTo(BigDecimal.ZERO) > 0) {
                profitLoss = currentValue.subtract(totalInvested);
                profitLossPercentage = profitLoss.divide(totalInvested, 4, BigDecimal.ROUND_HALF_UP)
                        .multiply(new BigDecimal(100)).doubleValue();
            }
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Portfolio getPortfolio() { return portfolio; }
    public void setPortfolio(Portfolio portfolio) { this.portfolio = portfolio; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public BigDecimal getAverageBuyPrice() { return averageBuyPrice; }
    public void setAverageBuyPrice(BigDecimal averageBuyPrice) { this.averageBuyPrice = averageBuyPrice; }

    public BigDecimal getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }

    public BigDecimal getTotalInvested() { return totalInvested; }
    public void setTotalInvested(BigDecimal totalInvested) { this.totalInvested = totalInvested; }

    public BigDecimal getCurrentValue() { return currentValue; }
    public void setCurrentValue(BigDecimal currentValue) { this.currentValue = currentValue; }

    public BigDecimal getProfitLoss() { return profitLoss; }
    public void setProfitLoss(BigDecimal profitLoss) { this.profitLoss = profitLoss; }

    public Double getProfitLossPercentage() { return profitLossPercentage; }
    public void setProfitLossPercentage(Double profitLossPercentage) { this.profitLossPercentage = profitLossPercentage; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
}