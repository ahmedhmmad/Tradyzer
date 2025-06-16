package com.tradyzer.bot;

import com.tradyzer.dto.TransactionRequest;
import com.tradyzer.entity.*;
import com.tradyzer.service.*;
import com.tradyzer.strategy.TradingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TradingBot {

    @Autowired
    private BinanceService binanceService;

    @Autowired
    private TechnicalAnalysisService technicalAnalysisService;

    @Autowired
    private PortfolioService portfolioService;

    @Autowired
    private List<TradingStrategy> strategies;

    private final Map<String, BotStatus> botStatuses = new ConcurrentHashMap<>();
    private boolean isActive = false;

    // تشغيل/إيقاف البوت
    public void start(Long portfolioId, String strategyName) {
        BotStatus status = new BotStatus();
        status.setPortfolioId(portfolioId);
        status.setStrategyName(strategyName);
        status.setActive(true);
        status.setStartedAt(LocalDateTime.now());

        botStatuses.put(portfolioId + "_" + strategyName, status);
        isActive = true;
    }

    public void stop(Long portfolioId, String strategyName) {
        String key = portfolioId + "_" + strategyName;
        BotStatus status = botStatuses.get(key);
        if (status != null) {
            status.setActive(false);
            status.setStoppedAt(LocalDateTime.now());
        }

        // إيقاف البوت إذا لم تعد هناك استراتيجيات نشطة
        isActive = botStatuses.values().stream().anyMatch(BotStatus::isActive);
    }

    // المهمة المجدولة - تعمل كل دقيقة
    @Scheduled(fixedDelay = 60000)
    public void executeTradingStrategies() {
        if (!isActive) {
            return;
        }

        System.out.println("🤖 Trading Bot: Analyzing market...");

        botStatuses.entrySet().stream()
                .filter(entry -> entry.getValue().isActive())
                .forEach(entry -> {
                    try {
                        executeStrategy(entry.getValue());
                    } catch (Exception e) {
                        System.err.println("Error executing strategy: " + e.getMessage());
                    }
                });
    }

    private void executeStrategy(BotStatus botStatus) {
        // العثور على الاستراتيجية
        TradingStrategy strategy = strategies.stream()
                .filter(s -> s.getName().equals(botStatus.getStrategyName()))
                .findFirst()
                .orElse(null);

        if (strategy == null) {
            return;
        }

        // تحليل كل عملة
        List<String> symbols = List.of("BTCUSDT", "ETHUSDT", "SOLUSDT", "XRPUSDT", "PEPEUSDT");

        for (String symbol : symbols) {
            TradingSignal signal = strategy.analyze(symbol);

            if (signal != null && signal.isExecutable()) {
                executeTrade(botStatus.getPortfolioId(), signal);
                botStatus.incrementTradeCount();
            }
        }
    }

    private void executeTrade(Long portfolioId, TradingSignal signal) {
        System.out.println("🚀 Executing trade: " + signal);

        TransactionRequest request = new TransactionRequest();
        request.setPortfolioId(portfolioId);
        request.setTransactionType(signal.getAction() == SignalAction.BUY ?
                TransactionType.BUY : TransactionType.SELL);
        request.setSymbol(signal.getSymbol());
        request.setQuantity(signal.getQuantity());
        request.setPrice(signal.getPrice());
        request.setNotes("Bot Trade - Strategy: " + signal.getStrategyName());

        // تنفيذ التداول
        // portfolioService.executeTransaction("bot_user", request);
    }

    public Map<String, BotStatus> getBotStatuses() {
        return botStatuses;
    }
}