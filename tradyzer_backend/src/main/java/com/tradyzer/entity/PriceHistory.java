package com.tradyzer.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "price_history")
public class PriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String symbol;

    @Column(precision = 20, scale = 8)
    private BigDecimal price;

    @Column(name = "price_change_percent")
    private Double priceChangePercent;

    @Column(precision = 20, scale = 2)
    private BigDecimal volume;

    @Column(name = "high_24h", precision = 20, scale = 8)
    private BigDecimal high24h;

    @Column(name = "low_24h", precision = 20, scale = 8)
    private BigDecimal low24h;

    @Column(name = "recorded_at")
    private LocalDateTime recordedAt;

    // Constructors
    public PriceHistory() {
        this.recordedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Double getPriceChangePercent() { return priceChangePercent; }
    public void setPriceChangePercent(Double priceChangePercent) { this.priceChangePercent = priceChangePercent; }

    public BigDecimal getVolume() { return volume; }
    public void setVolume(BigDecimal volume) { this.volume = volume; }

    public BigDecimal getHigh24h() { return high24h; }
    public void setHigh24h(BigDecimal high24h) { this.high24h = high24h; }

    public BigDecimal getLow24h() { return low24h; }
    public void setLow24h(BigDecimal low24h) { this.low24h = low24h; }

    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
}