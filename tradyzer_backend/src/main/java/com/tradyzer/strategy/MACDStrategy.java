package com.tradyzer.strategy;

import com.tradyzer.bot.SignalAction;
import com.tradyzer.bot.TradingSignal;
import com.tradyzer.service.TechnicalAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Component
public class MACDStrategy implements TradingStrategy {

    @Autowired
    private TechnicalAnalysisService technicalAnalysisService;

    @Override
    public String getName() {
        return "MACD_STRATEGY";
    }

    @Override
    public String getDescription() {
        return "Buy/Sell based on MACD crossovers";
    }

    @Override
    public TradingSignal analyze(String symbol) {
        TradingSignal signal = new TradingSignal();
        signal.setSymbol(symbol);
        signal.setStrategyName(getName());
        signal.setTimestamp(LocalDateTime.now());

        Map<String, Double> macd = technicalAnalysisService.calculateMACD(symbol);
        if (macd == null) {
            signal.setAction(SignalAction.HOLD);
            signal.setConfidence(0);
            return signal;
        }

        Double histogram = macd.get("histogram");

        if (histogram > 0) {
            signal.setAction(SignalAction.BUY);
            signal.setReason("MACD Bullish Crossover");
            signal.setConfidence(75);
        } else if (histogram < 0) {
            signal.setAction(SignalAction.SELL);
            signal.setReason("MACD Bearish Crossover");
            signal.setConfidence(75);
        } else {
            signal.setAction(SignalAction.HOLD);
            signal.setConfidence(50);
        }

        return signal;
    }

    @Override
    public double getMinimumBalance() {
        return 2000;
    }

    @Override
    public double getRiskLevel() {
        return 6;
    }
}