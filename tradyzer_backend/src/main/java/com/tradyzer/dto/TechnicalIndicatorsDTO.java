package com.tradyzer.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public class TechnicalIndicatorsDTO {
    private String symbol;
    private Double rsi14;
    private Map<String, Double> macd;
    private Map<String, BigDecimal> bollingerBands;
    private Map<String, BigDecimal> movingAverages;
    private String signal; // BUY, SELL, HOLD
    private LocalDateTime calculatedAt;

    // Getters and Setters
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public Double getRsi14() { return rsi14; }
    public void setRsi14(Double rsi14) { this.rsi14 = rsi14; }

    public Map<String, Double> getMacd() { return macd; }
    public void setMacd(Map<String, Double> macd) { this.macd = macd; }

    public Map<String, BigDecimal> getBollingerBands() { return bollingerBands; }
    public void setBollingerBands(Map<String, BigDecimal> bollingerBands) { this.bollingerBands = bollingerBands; }

    public Map<String, BigDecimal> getMovingAverages() { return movingAverages; }
    public void setMovingAverages(Map<String, BigDecimal> movingAverages) { this.movingAverages = movingAverages; }

    public String getSignal() { return signal; }
    public void setSignal(String signal) { this.signal = signal; }

    public LocalDateTime getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(LocalDateTime calculatedAt) { this.calculatedAt = calculatedAt; }
}