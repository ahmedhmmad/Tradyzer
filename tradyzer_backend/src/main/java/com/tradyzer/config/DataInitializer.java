package com.tradyzer.config;

import com.tradyzer.entity.CryptoCurrency;
import com.tradyzer.service.CryptoCurrencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Configuration
public class DataInitializer {

    @Autowired
    private CryptoCurrencyService cryptoCurrencyService;

    @Bean
    CommandLineRunner initDatabase() {
        return args -> {
            // التحقق من وجود بيانات
            if (cryptoCurrencyService.getAllCurrencies().isEmpty()) {
                System.out.println("Initializing cryptocurrency data...");

                List<CryptoCurrency> currencies = Arrays.asList(
                        createCrypto("BTCUSDT", "Bitcoin", new BigDecimal("65000"), true),
                        createCrypto("ETHUSDT", "Ethereum", new BigDecimal("3500"), true),
                        createCrypto("SOLUSDT", "Solana", new BigDecimal("150"), true),
                        createCrypto("XRPUSDT", "Ripple", new BigDecimal("0.65"), true),
                        createCrypto("PEPEUSDT", "Pepe", new BigDecimal("0.000012"), true)
                );

                currencies.forEach(cryptoCurrencyService::saveCurrency);
                System.out.println("Cryptocurrency data initialized successfully!");
            }
        };
    }

    private CryptoCurrency createCrypto(String symbol, String name, BigDecimal price, boolean tracking) {
        CryptoCurrency crypto = new CryptoCurrency();
        crypto.setSymbol(symbol);
        crypto.setName(name);
        crypto.setCurrentPrice(price);
        crypto.setActive(true);
        crypto.setTracking(tracking);
        crypto.setVolume24h(new BigDecimal("1000000"));
        crypto.setMarketCap(new BigDecimal("10000000"));
        return crypto;
    }
}