package com.tradyzer.websocket;

import com.tradyzer.service.BinanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

@Controller
public class PriceWebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private BinanceService binanceService;

    // إرسال تحديثات الأسعار كل 5 ثواني
    @Scheduled(fixedDelay = 5000)
    public void sendPriceUpdates() {
        List<Map<String, Object>> prices = binanceService.getRequiredCryptos();
        messagingTemplate.convertAndSend("/topic/prices", prices);
    }

    // إرسال تحديث لعملة محددة
    public void sendPriceUpdate(String symbol, Map<String, Object> priceData) {
        messagingTemplate.convertAndSend("/topic/price/" + symbol, priceData);
    }
}