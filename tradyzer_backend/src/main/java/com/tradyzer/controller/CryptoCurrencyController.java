package com.tradyzer.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/cryptocurrencies")
public class CryptoCurrencyController {

    @GetMapping
    public List<Map<String, Object>> getAllCurrencies() {
        // بيانات مؤقتة للاختبار
        List<Map<String, Object>> currencies = new ArrayList<>();

        Map<String, Object> btc = new HashMap<>();
        btc.put("symbol", "BTCUSDT");
        btc.put("name", "Bitcoin");
        btc.put("price", 65000);
        currencies.add(btc);

        Map<String, Object> eth = new HashMap<>();
        eth.put("symbol", "ETHUSDT");
        eth.put("name", "Ethereum");
        eth.put("price", 3500);
        currencies.add(eth);

        return currencies;
    }

    @GetMapping("/test")
    public Map<String, String> test() {
        return Map.of("message", "CryptoCurrency Controller is working!");
    }
}