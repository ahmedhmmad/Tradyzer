package com.tradyzer.controller;

import com.tradyzer.entity.PriceHistory;
import com.tradyzer.repository.PriceHistoryRepository;
import com.tradyzer.service.BinanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    @Autowired
    private BinanceService binanceService;

    @Autowired
    private PriceHistoryRepository priceHistoryRepository;

    // بيانات لوحة التحكم الرئيسية
    @GetMapping
    public ResponseEntity<Map<String, Object>> getDashboardData() {
        Map<String, Object> dashboard = new HashMap<>();

        // الأسعار الحالية
        dashboard.put("currentPrices", binanceService.getRequiredCryptos());

        // أفضل الرابحين والخاسرين
        List<Map<String, Object>> allPrices = binanceService.getRequiredCryptos();

        // ترتيب حسب نسبة التغيير
        allPrices.sort((a, b) -> {
            Double changeA = Double.parseDouble(a.get("priceChangePercent").toString());
            Double changeB = Double.parseDouble(b.get("priceChangePercent").toString());
            return changeB.compareTo(changeA);
        });

        dashboard.put("topGainer", allPrices.get(0));
        dashboard.put("topLoser", allPrices.get(allPrices.size() - 1));

        // إحصائيات
        dashboard.put("totalCurrencies", allPrices.size());
        dashboard.put("lastUpdate", LocalDateTime.now());

        return ResponseEntity.ok(dashboard);
    }

    // بيانات تاريخية لعملة
    @GetMapping("/history/{symbol}")
    public ResponseEntity<List<PriceHistory>> getPriceHistory(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "24") int hours) {

        LocalDateTime startTime = LocalDateTime.now().minusHours(hours);
        List<PriceHistory> history = priceHistoryRepository.findBySymbolAndTimeRange(symbol, startTime);

        return ResponseEntity.ok(history);
    }

    // ملخص السوق
    @GetMapping("/market-summary")
    public ResponseEntity<Map<String, Object>> getMarketSummary() {
        Map<String, Object> summary = new HashMap<>();

        List<Map<String, Object>> top10 = binanceService.getTopCryptos();
        summary.put("top10ByVolume", top10);

        // حساب إجمالي حجم التداول
        double totalVolume = top10.stream()
                .mapToDouble(crypto -> ((BigDecimal) crypto.get("volume")).doubleValue())
                .sum();

        summary.put("totalMarketVolume", totalVolume);
        summary.put("activeMarkets", top10.size());

        return ResponseEntity.ok(summary);
    }
}