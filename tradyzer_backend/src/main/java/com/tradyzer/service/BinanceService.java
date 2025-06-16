package com.tradyzer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradyzer.entity.PriceHistory;
import com.tradyzer.repository.PriceHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BinanceService {

    private final WebClient binanceWebClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public BinanceService(WebClient binanceWebClient) {
        this.binanceWebClient = binanceWebClient;
        this.objectMapper = new ObjectMapper();
    }
    @Autowired
    private PriceHistoryRepository priceHistoryRepository;

    // حفظ سعر في قاعدة البيانات
    public PriceHistory savePriceHistory(Map<String, Object> tickerData) {
        PriceHistory history = new PriceHistory();
        history.setSymbol((String) tickerData.get("symbol"));
        history.setPrice(new BigDecimal(tickerData.get("lastPrice").toString()));
        history.setPriceChangePercent(Double.parseDouble(tickerData.get("priceChangePercent").toString()));
        history.setVolume(new BigDecimal(tickerData.get("quoteVolume").toString()));
        history.setHigh24h(new BigDecimal(tickerData.get("highPrice").toString()));
        history.setLow24h(new BigDecimal(tickerData.get("lowPrice").toString()));

        return priceHistoryRepository.save(history);
    }

    // تحديث الأسعار وحفظها
    public void updateAndSavePrices() {
        List<Map<String, Object>> cryptos = getRequiredCryptos();

        for (Map<String, Object> crypto : cryptos) {
            if (!crypto.containsKey("error")) {
                savePriceHistory(crypto);
            }
        }
    }

    // الحصول على سعر عملة واحدة
    public Map<String, Object> getPrice(String symbol) {
        try {
            String response = binanceWebClient
                    .get()
                    .uri("/api/v3/ticker/price?symbol=" + symbol.toUpperCase())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode jsonNode = objectMapper.readTree(response);

            Map<String, Object> result = new HashMap<>();
            result.put("symbol", jsonNode.get("symbol").asText());
            result.put("price", new BigDecimal(jsonNode.get("price").asText()));
            result.put("timestamp", new Date());

            return result;
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to fetch price for " + symbol);
            error.put("message", e.getMessage());
            return error;
        }
    }

    // الحصول على معلومات 24 ساعة
    public Map<String, Object> get24hrTicker(String symbol) {
        try {
            String response = binanceWebClient
                    .get()
                    .uri("/api/v3/ticker/24hr?symbol=" + symbol.toUpperCase())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode jsonNode = objectMapper.readTree(response);

            Map<String, Object> result = new HashMap<>();
            result.put("symbol", jsonNode.get("symbol").asText());
            result.put("priceChange", jsonNode.get("priceChange").asText());
            result.put("priceChangePercent", jsonNode.get("priceChangePercent").asText());
            result.put("lastPrice", new BigDecimal(jsonNode.get("lastPrice").asText()));
            result.put("openPrice", jsonNode.get("openPrice").asText());
            result.put("highPrice", jsonNode.get("highPrice").asText());
            result.put("lowPrice", jsonNode.get("lowPrice").asText());
            result.put("volume", jsonNode.get("volume").asText());
            result.put("quoteVolume", jsonNode.get("quoteVolume").asText());

            return result;
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to fetch 24hr ticker for " + symbol);
            error.put("message", e.getMessage());
            return error;
        }
    }

    // الحصول على أسعار متعددة
    public List<Map<String, Object>> getMultiplePrices(List<String> symbols) {
        List<Map<String, Object>> prices = new ArrayList<>();

        for (String symbol : symbols) {
            prices.add(getPrice(symbol));
        }

        return prices;
    }

    // الحصول على أفضل 10 عملات حسب الحجم
    public List<Map<String, Object>> getTopCryptos() {
        try {
            String response = binanceWebClient
                    .get()
                    .uri("/api/v3/ticker/24hr")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode jsonArray = objectMapper.readTree(response);
            List<Map<String, Object>> allTickers = new ArrayList<>();

            for (JsonNode node : jsonArray) {
                String symbol = node.get("symbol").asText();
                // فلترة العملات مقابل USDT فقط
                if (symbol.endsWith("USDT")) {
                    Map<String, Object> ticker = new HashMap<>();
                    ticker.put("symbol", symbol);
                    ticker.put("lastPrice", new BigDecimal(node.get("lastPrice").asText()));
                    ticker.put("priceChangePercent", Double.parseDouble(node.get("priceChangePercent").asText()));
                    ticker.put("volume", new BigDecimal(node.get("quoteVolume").asText()));
                    allTickers.add(ticker);
                }
            }

            // ترتيب حسب الحجم وأخذ أعلى 10
            return allTickers.stream()
                    .sorted((a, b) -> {
                        BigDecimal volA = (BigDecimal) a.get("volume");
                        BigDecimal volB = (BigDecimal) b.get("volume");
                        return volB.compareTo(volA);
                    })
                    .limit(10)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // الحصول على العملات المطلوبة (BTC, ETH, SOL, XRP, PEPE)
    public List<Map<String, Object>> getRequiredCryptos() {
        List<String> symbols = Arrays.asList(
                "BTCUSDT",
                "ETHUSDT",
                "SOLUSDT",
                "XRPUSDT",
                "PEPEUSDT"
        );

        List<Map<String, Object>> result = new ArrayList<>();

        for (String symbol : symbols) {
            Map<String, Object> data = get24hrTicker(symbol);
            if (!data.containsKey("error")) {
                // إضافة اسم العملة
                data.put("name", getNameFromSymbol(symbol));
                result.add(data);
            }
        }

        return result;
    }

    private String getNameFromSymbol(String symbol) {
        Map<String, String> names = new HashMap<>();
        names.put("BTCUSDT", "Bitcoin");
        names.put("ETHUSDT", "Ethereum");
        names.put("SOLUSDT", "Solana");
        names.put("XRPUSDT", "Ripple");
        names.put("PEPEUSDT", "Pepe");

        return names.getOrDefault(symbol, symbol.replace("USDT", ""));
    }
}