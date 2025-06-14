package com.tradyzer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class PriceUpdateService {

    private final BinanceService binanceService;

    @Autowired
    public PriceUpdateService(BinanceService binanceService) {
        this.binanceService = binanceService;
    }

    // تحديث الأسعار كل 10 ثواني
    @Scheduled(fixedDelay = 10000)
    public void updatePrices() {
        System.out.println("Updating cryptocurrency prices...");

        List<Map<String, Object>> prices = binanceService.getRequiredCryptos();

        for (Map<String, Object> crypto : prices) {
            System.out.printf("%s: $%.2f (%.2f%%)%n",
                    crypto.get("symbol"),
                    crypto.get("lastPrice"),
                    crypto.get("priceChangePercent")
            );
        }

        System.out.println("----------------------------");
    }
}