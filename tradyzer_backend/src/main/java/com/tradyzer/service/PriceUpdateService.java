package com.tradyzer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class PriceUpdateService {

    private final BinanceService binanceService;
    private final AlertService alertService;

    @Autowired
    public PriceUpdateService(BinanceService binanceService, AlertService alertService) {
        this.binanceService = binanceService;
        this.alertService = alertService;
    }

    // عرض الأسعار في Console كل 10 ثواني
    @Scheduled(fixedDelay = 10000)
    public void displayPrices() {
        System.out.println("=== Cryptocurrency Prices Update ===");

        List<Map<String, Object>> prices = binanceService.getRequiredCryptos();

        for (Map<String, Object> crypto : prices) {
            System.out.printf("%-10s: $%-10.2f (%-6.2f%%)%n",
                    crypto.get("symbol"),
                    crypto.get("lastPrice"),
                    crypto.get("priceChangePercent")
            );
        }

        System.out.println("===================================\n");
    }

    // تحديث وحفظ الأسعار في قاعدة البيانات كل 30 ثانية
    @Scheduled(fixedDelay = 30000)
    public void updateAndSavePrices() {
        System.out.println("💾 Saving prices to database...");

        try {
            binanceService.updateAndSavePrices();
            System.out.println("✅ Prices saved successfully!");
        } catch (Exception e) {
            System.err.println("❌ Error saving prices: " + e.getMessage());
        }
    }

    // فحص التنبيهات كل دقيقة
    @Scheduled(fixedDelay = 60000)
    public void checkAlerts() {
        System.out.println("🔔 Checking price alerts...");

        try {
            alertService.checkAlerts();
            System.out.println("✅ Alerts checked!");
        } catch (Exception e) {
            System.err.println("❌ Error checking alerts: " + e.getMessage());
        }
    }

    // تحديث مفصل كل 5 دقائق
    @Scheduled(fixedDelay = 300000) // 5 minutes
    public void detailedMarketUpdate() {
        System.out.println("\n📊 === DETAILED MARKET UPDATE === 📊");

        // أفضل 10 عملات
        List<Map<String, Object>> top10 = binanceService.getTopCryptos();

        System.out.println("\n🏆 Top 10 Cryptocurrencies by Volume:");
        System.out.println("----------------------------------------");

        int rank = 1;
        for (Map<String, Object> crypto : top10) {
            System.out.printf("%2d. %-12s: $%-10.2f Volume: $%-15.0f%n",
                    rank++,
                    crypto.get("symbol"),
                    crypto.get("lastPrice"),
                    crypto.get("volume")
            );
        }

        System.out.println("========================================\n");
    }
}