package com.tradyzer.service;

import com.tradyzer.dto.TechnicalIndicatorsDTO;
import com.tradyzer.entity.PriceHistory;
import com.tradyzer.repository.PriceHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TechnicalAnalysisService {

    @Autowired
    private PriceHistoryRepository priceHistoryRepository;

    // حساب RSI (Relative Strength Index)
    public Double calculateRSI(String symbol, int period) {
        List<PriceHistory> prices = priceHistoryRepository
                .findBySymbolOrderByRecordedAtDesc(symbol)
                .stream()
                .limit(period + 1)
                .collect(Collectors.toList());

        if (prices.size() < period + 1) {
            return null;
        }

        Collections.reverse(prices);

        double avgGain = 0;
        double avgLoss = 0;

        // حساب المتوسط الأولي
        for (int i = 1; i <= period; i++) {
            double change = prices.get(i).getPrice().subtract(prices.get(i-1).getPrice()).doubleValue();
            if (change > 0) {
                avgGain += change;
            } else {
                avgLoss += Math.abs(change);
            }
        }

        avgGain /= period;
        avgLoss /= period;

        if (avgLoss == 0) {
            return 100.0;
        }

        double rs = avgGain / avgLoss;
        double rsi = 100 - (100 / (1 + rs));

        return Math.round(rsi * 100.0) / 100.0;
    }

    // حساب MACD (Moving Average Convergence Divergence)
    public Map<String, Double> calculateMACD(String symbol) {
        List<PriceHistory> prices = priceHistoryRepository
                .findBySymbolOrderByRecordedAtDesc(symbol)
                .stream()
                .limit(50)
                .collect(Collectors.toList());

        if (prices.size() < 26) {
            return null;
        }

        Collections.reverse(prices);

        // حساب EMA 12
        double ema12 = calculateEMA(prices.stream()
                .map(p -> p.getPrice().doubleValue())
                .collect(Collectors.toList()), 12);

        // حساب EMA 26
        double ema26 = calculateEMA(prices.stream()
                .map(p -> p.getPrice().doubleValue())
                .collect(Collectors.toList()), 26);

        // MACD Line
        double macdLine = ema12 - ema26;

        // Signal Line (9-day EMA of MACD)
        double signalLine = macdLine * 0.9; // تبسيط

        // MACD Histogram
        double histogram = macdLine - signalLine;

        Map<String, Double> macd = new HashMap<>();
        macd.put("macdLine", Math.round(macdLine * 100.0) / 100.0);
        macd.put("signalLine", Math.round(signalLine * 100.0) / 100.0);
        macd.put("histogram", Math.round(histogram * 100.0) / 100.0);

        return macd;
    }

    // حساب Bollinger Bands
    public Map<String, BigDecimal> calculateBollingerBands(String symbol, int period) {
        List<PriceHistory> prices = priceHistoryRepository
                .findBySymbolOrderByRecordedAtDesc(symbol)
                .stream()
                .limit(period)
                .collect(Collectors.toList());

        if (prices.size() < period) {
            return null;
        }

        // حساب SMA
        BigDecimal sum = prices.stream()
                .map(PriceHistory::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal sma = sum.divide(new BigDecimal(period), 8, RoundingMode.HALF_UP);

        // حساب الانحراف المعياري
        double variance = 0;
        for (PriceHistory price : prices) {
            double diff = price.getPrice().subtract(sma).doubleValue();
            variance += diff * diff;
        }
        variance /= period;
        double stdDev = Math.sqrt(variance);

        BigDecimal stdDevBD = new BigDecimal(stdDev);
        BigDecimal multiplier = new BigDecimal(2);

        Map<String, BigDecimal> bands = new HashMap<>();
        bands.put("upper", sma.add(stdDevBD.multiply(multiplier)));
        bands.put("middle", sma);
        bands.put("lower", sma.subtract(stdDevBD.multiply(multiplier)));

        return bands;
    }

    // حساب Moving Averages
    public Map<String, BigDecimal> calculateMovingAverages(String symbol) {
        Map<String, BigDecimal> averages = new HashMap<>();

        // SMA 50
        BigDecimal sma50 = calculateSMA(symbol, 50);
        if (sma50 != null) averages.put("sma50", sma50);

        // SMA 200
        BigDecimal sma200 = calculateSMA(symbol, 200);
        if (sma200 != null) averages.put("sma200", sma200);

        // EMA 20
        BigDecimal ema20 = calculateEMAFromDB(symbol, 20);
        if (ema20 != null) averages.put("ema20", ema20);

        return averages;
    }

    // حساب SMA
    private BigDecimal calculateSMA(String symbol, int period) {
        List<PriceHistory> prices = priceHistoryRepository
                .findBySymbolOrderByRecordedAtDesc(symbol)
                .stream()
                .limit(period)
                .collect(Collectors.toList());

        if (prices.size() < period) {
            return null;
        }

        BigDecimal sum = prices.stream()
                .map(PriceHistory::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return sum.divide(new BigDecimal(period), 8, RoundingMode.HALF_UP);
    }

    // حساب EMA
    private double calculateEMA(List<Double> prices, int period) {
        if (prices.size() < period) {
            return 0;
        }

        double multiplier = 2.0 / (period + 1);
        double ema = prices.subList(0, period).stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0);

        for (int i = period; i < prices.size(); i++) {
            ema = (prices.get(i) - ema) * multiplier + ema;
        }

        return ema;
    }

    private BigDecimal calculateEMAFromDB(String symbol, int period) {
        List<PriceHistory> prices = priceHistoryRepository
                .findBySymbolOrderByRecordedAtDesc(symbol)
                .stream()
                .limit(period)
                .collect(Collectors.toList());

        if (prices.size() < period) {
            return null;
        }

        Collections.reverse(prices);

        double ema = calculateEMA(prices.stream()
                .map(p -> p.getPrice().doubleValue())
                .collect(Collectors.toList()), period);

        return new BigDecimal(ema).setScale(8, RoundingMode.HALF_UP);
    }

    // الحصول على جميع المؤشرات
    public TechnicalIndicatorsDTO getAllIndicators(String symbol) {
        TechnicalIndicatorsDTO indicators = new TechnicalIndicatorsDTO();
        indicators.setSymbol(symbol);

        // RSI
        indicators.setRsi14(calculateRSI(symbol, 14));

        // MACD
        indicators.setMacd(calculateMACD(symbol));

        // Bollinger Bands
        indicators.setBollingerBands(calculateBollingerBands(symbol, 20));

        // Moving Averages
        indicators.setMovingAverages(calculateMovingAverages(symbol));

        // Trading Signal
        indicators.setSignal(generateTradingSignal(indicators));

        indicators.setCalculatedAt(LocalDateTime.now());

        return indicators;
    }

    // توليد إشارة التداول
    private String generateTradingSignal(TechnicalIndicatorsDTO indicators) {
        int bullishSignals = 0;
        int bearishSignals = 0;

        // RSI Signal
        if (indicators.getRsi14() != null) {
            if (indicators.getRsi14() < 30) {
                bullishSignals++; // Oversold
            } else if (indicators.getRsi14() > 70) {
                bearishSignals++; // Overbought
            }
        }

        // MACD Signal
        if (indicators.getMacd() != null) {
            Double histogram = indicators.getMacd().get("histogram");
            if (histogram != null && histogram > 0) {
                bullishSignals++;
            } else if (histogram != null && histogram < 0) {
                bearishSignals++;
            }
        }

        // تحديد الإشارة
        if (bullishSignals > bearishSignals) {
            return "BUY";
        } else if (bearishSignals > bullishSignals) {
            return "SELL";
        } else {
            return "HOLD";
        }
    }
}