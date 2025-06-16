package com.tradyzer.strategy;

import com.tradyzer.bot.SignalAction;
import com.tradyzer.bot.TradingSignal;
import com.tradyzer.service.BinanceService;
import com.tradyzer.service.TechnicalAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Component
public class RSIStrategy implements TradingStrategy {

    @Autowired
    private TechnicalAnalysisService technicalAnalysisService;

    @Autowired
    private BinanceService binanceService;

    private final double RSI_OVERSOLD = 30;
    private final double RSI_OVERBOUGHT = 70;

    @Override
    public String getName() {
        return "RSI_STRATEGY";
    }

    @Override
    public String getDescription() {
        return "Buy when RSI < 30 (oversold), Sell when RSI > 70 (overbought)";
    }

    @Override
    public TradingSignal analyze(String symbol) {
        TradingSignal signal = new TradingSignal();
        signal.setSymbol(symbol);
        signal.setStrategyName(getName());
        signal.setTimestamp(LocalDateTime.now());

        // حساب RSI
        Double rsi = technicalAnalysisService.calculateRSI(symbol, 14);
        if (rsi == null) {
            signal.setAction(SignalAction.HOLD);
            signal.setReason("Insufficient data for RSI calculation");
            signal.setConfidence(0);
            return signal;
        }

        // الحصول على السعر الحالي
        Map<String, Object> priceData = binanceService.getPrice(symbol);
        BigDecimal currentPrice = (BigDecimal) priceData.get("price");
        signal.setPrice(currentPrice);

        // تحديد الإشارة
        if (rsi < RSI_OVERSOLD) {
            signal.setAction(SignalAction.BUY);
            signal.setReason(String.format("RSI = %.2f (Oversold)", rsi));
            signal.setConfidence(80 + (RSI_OVERSOLD - rsi)); // كلما انخفض RSI، زادت الثقة
            signal.setQuantity(calculatePositionSize(currentPrice));
        } else if (rsi > RSI_OVERBOUGHT) {
            signal.setAction(SignalAction.SELL);
            signal.setReason(String.format("RSI = %.2f (Overbought)", rsi));
            signal.setConfidence(80 + (rsi - RSI_OVERBOUGHT));
            signal.setQuantity(calculatePositionSize(currentPrice));
        } else {
            signal.setAction(SignalAction.HOLD);
            signal.setReason(String.format("RSI = %.2f (Neutral)", rsi));
            signal.setConfidence(50);
        }

        return signal;
    }

    private BigDecimal calculatePositionSize(BigDecimal price) {
        // حجم المركز = 1% من رأس المال
        // هذا مثال بسيط، يجب تحسينه حسب حجم المحفظة
        return new BigDecimal("100").divide(price, 8, BigDecimal.ROUND_HALF_UP);
    }

    @Override
    public double getMinimumBalance() {
        return 1000; // $1000 minimum
    }

    @Override
    public double getRiskLevel() {
        return 5; // متوسط المخاطر
    }
}