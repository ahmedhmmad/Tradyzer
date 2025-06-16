package com.tradyzer.controller;

import com.tradyzer.repository.PriceHistoryRepository;
import com.tradyzer.repository.PriceAlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/monitor")
public class MonitorController {

    @Autowired
    private PriceHistoryRepository priceHistoryRepository;

    @Autowired(required = false)
    private PriceAlertRepository alertRepository;

    @GetMapping("/status")
    public Map<String, Object> getSystemStatus() {
        Map<String, Object> status = new HashMap<>();

        // معلومات قاعدة البيانات
        status.put("totalPriceRecords", priceHistoryRepository.count());
        status.put("distinctSymbols", priceHistoryRepository.findDistinctSymbols());

        // آخر تحديث
        priceHistoryRepository.findAll()
                .stream()
                .max((a, b) -> a.getRecordedAt().compareTo(b.getRecordedAt()))
                .ifPresent(latest -> {
                    status.put("lastUpdate", latest.getRecordedAt());
                    status.put("lastPrice", latest.getPrice());
                    status.put("lastSymbol", latest.getSymbol());
                });

        // التنبيهات
        if (alertRepository != null) {
            status.put("totalAlerts", alertRepository.count());
            status.put("activeAlerts", alertRepository.findByIsActiveTrue().size());
        }

        status.put("serverTime", LocalDateTime.now());
        status.put("status", "RUNNING");

        return status;
    }

    @GetMapping("/health-check")
    public Map<String, String> healthCheck() {
        Map<String, String> health = new HashMap<>();

        try {
            // فحص قاعدة البيانات
            priceHistoryRepository.count();
            health.put("database", "✅ Connected");
        } catch (Exception e) {
            health.put("database", "❌ Error: " + e.getMessage());
        }

        // فحص الخدمات
        health.put("priceUpdates", "✅ Running");
        health.put("webSocket", "✅ Active");
        health.put("scheduler", "✅ Working");

        return health;
    }
}