package com.tradyzer.bot;

import java.time.LocalDateTime;

public class BotStatus {
    private Long portfolioId;
    private String strategyName;
    private boolean isActive;
    private LocalDateTime startedAt;
    private LocalDateTime stoppedAt;
    private int tradeCount;
    private double totalProfit;

    // Getters and Setters
    public Long getPortfolioId() { return portfolioId; }
    public void setPortfolioId(Long portfolioId) { this.portfolioId = portfolioId; }

    public String getStrategyName() { return strategyName; }
    public void setStrategyName(String strategyName) { this.strategyName = strategyName; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getStoppedAt() { return stoppedAt; }
    public void setStoppedAt(LocalDateTime stoppedAt) { this.stoppedAt = stoppedAt; }

    public int getTradeCount() { return tradeCount; }
    public void incrementTradeCount() { this.tradeCount++; }

    public double getTotalProfit() { return totalProfit; }
    public void addProfit(double profit) { this.totalProfit += profit; }
}