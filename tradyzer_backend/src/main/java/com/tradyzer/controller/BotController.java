package com.tradyzer.controller;

import com.tradyzer.bot.BotStatus;
import com.tradyzer.bot.TradingBot;
import com.tradyzer.risk.RiskAssessment;
import com.tradyzer.risk.RiskManager;
import com.tradyzer.service.PortfolioService;
import com.tradyzer.strategy.TradingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/bot")
public class BotController {

    @Autowired
    private TradingBot tradingBot;

    @Autowired
    private RiskManager riskManager;

    @Autowired
    private PortfolioService portfolioService;

    @Autowired
    private List<TradingStrategy> strategies;

    // الحصول على الاستراتيجيات المتاحة
    @GetMapping("/strategies")
    public ResponseEntity<List<Map<String, Object>>> getAvailableStrategies() {
        List<Map<String, Object>> strategyList = new ArrayList<>();

        for (TradingStrategy strategy : strategies) {
            Map<String, Object> strategyInfo = new HashMap<>();
            strategyInfo.put("name", strategy.getName());
            strategyInfo.put("description", strategy.getDescription());
            strategyInfo.put("minimumBalance", strategy.getMinimumBalance());
            strategyInfo.put("riskLevel", strategy.getRiskLevel());

            strategyList.add(strategyInfo);
        }

        return ResponseEntity.ok(strategyList);
    }

    // تشغيل البوت
    @PostMapping("/start")
    public ResponseEntity<Map<String, String>> startBot(
            Authentication auth,
            @RequestBody Map<String, Object> request) {

        Long portfolioId = Long.valueOf(request.get("portfolioId").toString());
        String strategyName = request.get("strategy").toString();

        // التحقق من ملكية المحفظة
        // TODO: Add ownership check

        tradingBot.start(portfolioId, strategyName);

        Map<String, String> response = new HashMap<>();
        response.put("status", "Bot started");
        response.put("portfolio", portfolioId.toString());
        response.put("strategy", strategyName);

        return ResponseEntity.ok(response);
    }

    // إيقاف البوت
    @PostMapping("/stop")
    public ResponseEntity<Map<String, String>> stopBot(
            Authentication auth,
            @RequestBody Map<String, Object> request) {

        Long portfolioId = Long.valueOf(request.get("portfolioId").toString());
        String strategyName = request.get("strategy").toString();

        tradingBot.stop(portfolioId, strategyName);

        Map<String, String> response = new HashMap<>();
        response.put("status", "Bot stopped");
        response.put("portfolio", portfolioId.toString());
        response.put("strategy", strategyName);

        return ResponseEntity.ok(response);
    }

    // حالة البوت
    @GetMapping("/status")
    public ResponseEntity<Map<String, BotStatus>> getBotStatus() {
        return ResponseEntity.ok(tradingBot.getBotStatuses());
    }

    // تقييم المخاطر
    @GetMapping("/risk-assessment/{portfolioId}")
    public ResponseEntity<RiskAssessment> getRiskAssessment(
            Authentication auth,
            @PathVariable Long portfolioId) {

        var portfolio = portfolioService.getPortfolioDetails(portfolioId, auth.getName());
        // RiskAssessment assessment = riskManager.assessPortfolioRisk(portfolio);

        // return ResponseEntity.ok(assessment);
        return ResponseEntity.ok(new RiskAssessment()); // مؤقت
    }
}