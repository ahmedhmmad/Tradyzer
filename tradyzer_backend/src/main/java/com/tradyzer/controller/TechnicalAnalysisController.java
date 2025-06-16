package com.tradyzer.controller;

import com.tradyzer.dto.TechnicalIndicatorsDTO;
import com.tradyzer.service.TechnicalAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/analysis")
@CrossOrigin(origins = "*")
public class TechnicalAnalysisController {

    @Autowired
    private TechnicalAnalysisService technicalAnalysisService;

    // الحصول على جميع المؤشرات
    @GetMapping("/{symbol}")
    public ResponseEntity<TechnicalIndicatorsDTO> getAllIndicators(@PathVariable String symbol) {
        TechnicalIndicatorsDTO indicators = technicalAnalysisService.getAllIndicators(symbol.toUpperCase());
        return ResponseEntity.ok(indicators);
    }

    // RSI
    @GetMapping("/{symbol}/rsi")
    public ResponseEntity<Map<String, Object>> getRSI(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "14") int period) {
        Double rsi = technicalAnalysisService.calculateRSI(symbol.toUpperCase(), period);
        return ResponseEntity.ok(Map.of(
                "symbol", symbol,
                "period", period,
                "rsi", rsi != null ? rsi : "Insufficient data"
        ));
    }

    // MACD
    @GetMapping("/{symbol}/macd")
    public ResponseEntity<Map<String, Object>> getMACD(@PathVariable String symbol) {
        Map<String, Double> macd = technicalAnalysisService.calculateMACD(symbol.toUpperCase());
        return ResponseEntity.ok(Map.of(
                "symbol", symbol,
                "macd", macd != null ? macd : "Insufficient data"
        ));
    }

    // Bollinger Bands
    @GetMapping("/{symbol}/bollinger")
    public ResponseEntity<Map<String, Object>> getBollingerBands(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "20") int period) {
        Map<String, BigDecimal> bands = technicalAnalysisService.calculateBollingerBands(symbol.toUpperCase(), period);
        return ResponseEntity.ok(Map.of(
                "symbol", symbol,
                "period", period,
                "bands", bands != null ? bands : "Insufficient data"
        ));
    }

    // Moving Averages
    @GetMapping("/{symbol}/ma")
    public ResponseEntity<Map<String, Object>> getMovingAverages(@PathVariable String symbol) {
        Map<String, BigDecimal> averages = technicalAnalysisService.calculateMovingAverages(symbol.toUpperCase());
        return ResponseEntity.ok(Map.of(
                "symbol", symbol,
                "movingAverages", averages
        ));
    }
}