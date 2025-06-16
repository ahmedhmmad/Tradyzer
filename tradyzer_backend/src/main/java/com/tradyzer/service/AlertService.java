package com.tradyzer.service;

import com.tradyzer.entity.AlertType;
import com.tradyzer.entity.PriceAlert;
import com.tradyzer.entity.User;
import com.tradyzer.repository.PriceAlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class AlertService {

    @Autowired
    private PriceAlertRepository alertRepository;

    @Autowired
    private BinanceService binanceService;

    // إنشاء تنبيه جديد
    public PriceAlert createAlert(User user, String symbol, BigDecimal targetPrice, AlertType type, String message) {
        PriceAlert alert = new PriceAlert();
        alert.setUser(user);
        alert.setSymbol(symbol.toUpperCase());
        alert.setTargetPrice(targetPrice);
        alert.setAlertType(type);
        alert.setMessage(message);

        return alertRepository.save(alert);
    }

    // فحص التنبيهات
    public void checkAlerts() {
        List<PriceAlert> activeAlerts = alertRepository.findByIsActiveTrue();

        for (PriceAlert alert : activeAlerts) {
            Map<String, Object> priceData = binanceService.getPrice(alert.getSymbol());

            if (!priceData.containsKey("error")) {
                BigDecimal currentPrice = (BigDecimal) priceData.get("price");

                boolean shouldTrigger = false;

                switch (alert.getAlertType()) {
                    case PRICE_ABOVE:
                        shouldTrigger = currentPrice.compareTo(alert.getTargetPrice()) >= 0;
                        break;
                    case PRICE_BELOW:
                        shouldTrigger = currentPrice.compareTo(alert.getTargetPrice()) <= 0;
                        break;
                }

                if (shouldTrigger) {
                    triggerAlert(alert, currentPrice);
                }
            }
        }
    }

    private void triggerAlert(PriceAlert alert, BigDecimal currentPrice) {
        alert.setTriggered(true);
        alert.setActive(false);
        alert.setTriggeredAt(LocalDateTime.now());

        alertRepository.save(alert);

        // هنا يمكن إرسال إشعار للمستخدم
        System.out.println("ALERT TRIGGERED: " + alert.getSymbol() +
                " reached " + currentPrice +
                " (Target: " + alert.getTargetPrice() + ")");
    }

    // الحصول على تنبيهات المستخدم
    public List<PriceAlert> getUserAlerts(User user) {
        return alertRepository.findByUser(user);
    }

    // إلغاء تنبيه
    public void cancelAlert(Long alertId) {
        alertRepository.findById(alertId).ifPresent(alert -> {
            alert.setActive(false);
            alertRepository.save(alert);
        });
    }
}