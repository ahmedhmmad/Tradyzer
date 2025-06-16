package com.tradyzer.strategy;

import com.tradyzer.bot.TradingSignal;

public interface TradingStrategy {
    String getName();
    String getDescription();
    TradingSignal analyze(String symbol);
    double getMinimumBalance();
    double getRiskLevel(); // 1-10
}