package com.tradyzer.bot;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TradingSignal {
    private String symbol;
    private SignalAction action;
    private BigDecimal price;
    private BigDecimal quantity;
    private String reason;
    private double confidence; // 0-100
    private String strategyName;
    private LocalDateTime timestamp;

    public boolean isExecutable() {
        return confidence > 70 && quantity != null && quantity.compareTo(BigDecimal.ZERO) > 0;
    }

    // Getters and Setters
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public SignalAction getAction() { return action; }
    public void setAction(SignalAction action) { this.action = action; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }

    public String getStrategyName() { return strategyName; }
    public void setStrategyName(String strategyName) { this.strategyName = strategyName; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return String.format("%s %s @ %.2f (Confidence: %.1f%%)",
                action, symbol, price, confidence);
    }
}

