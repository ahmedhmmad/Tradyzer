package com.tradyzer.controller;

import com.tradyzer.service.BinanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/binance")
@CrossOrigin(origins = "*")
public class BinanceController {

    private final BinanceService binanceService;

    @Autowired
    public BinanceController(BinanceService binanceService) {
        this.binanceService = binanceService;
    }

    // الحصول على سعر عملة واحدة
    @GetMapping("/price/{symbol}")
    public ResponseEntity<Map<String, Object>> getPrice(@PathVariable String symbol) {
        Map<String, Object> price = binanceService.getPrice(symbol);
        return ResponseEntity.ok(price);
    }

    // الحصول على معلومات 24 ساعة
    @GetMapping("/ticker/{symbol}")
    public ResponseEntity<Map<String, Object>> get24hrTicker(@PathVariable String symbol) {
        Map<String, Object> ticker = binanceService.get24hrTicker(symbol);
        return ResponseEntity.ok(ticker);
    }

    // الحصول على أسعار متعددة
    @PostMapping("/prices")
    public ResponseEntity<List<Map<String, Object>>> getMultiplePrices(@RequestBody List<String> symbols) {
        List<Map<String, Object>> prices = binanceService.getMultiplePrices(symbols);
        return ResponseEntity.ok(prices);
    }

    // الحصول على العملات المطلوبة
    @GetMapping("/required")
    public ResponseEntity<List<Map<String, Object>>> getRequiredCryptos() {
        List<Map<String, Object>> cryptos = binanceService.getRequiredCryptos();
        return ResponseEntity.ok(cryptos);
    }

    // الحصول على أفضل 10 عملات
    @GetMapping("/top10")
    public ResponseEntity<List<Map<String, Object>>> getTop10() {
        List<Map<String, Object>> top10 = binanceService.getTopCryptos();
        return ResponseEntity.ok(top10);
    }

    // اختبار الاتصال
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> test() {
        return ResponseEntity.ok(Map.of(
                "status", "Binance API Connected",
                "timestamp", System.currentTimeMillis()
        ));
    }
}