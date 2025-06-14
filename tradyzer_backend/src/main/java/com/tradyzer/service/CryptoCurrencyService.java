package com.tradyzer.service;

import com.tradyzer.entity.CryptoCurrency;
import com.tradyzer.repository.CryptoCurrencyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CryptoCurrencyService {

    private final CryptoCurrencyRepository cryptoCurrencyRepository;

    @Autowired
    public CryptoCurrencyService(CryptoCurrencyRepository cryptoCurrencyRepository) {
        this.cryptoCurrencyRepository = cryptoCurrencyRepository;
    }

    // الحصول على جميع العملات
    public List<CryptoCurrency> getAllCurrencies() {
        return cryptoCurrencyRepository.findAll();
    }

    // الحصول على العملات النشطة
    public List<CryptoCurrency> getActiveCurrencies() {
        return cryptoCurrencyRepository.findByIsActiveTrue();
    }

    // الحصول على العملات المتابعة
    public List<CryptoCurrency> getTrackingCurrencies() {
        return cryptoCurrencyRepository.findByIsTrackingTrue();
    }

    // الحصول على عملة بواسطة الرمز
    public Optional<CryptoCurrency> getCurrencyBySymbol(String symbol) {
        return cryptoCurrencyRepository.findById(symbol.toUpperCase());
    }

    // إضافة أو تحديث عملة
    public CryptoCurrency saveCurrency(CryptoCurrency currency) {
        currency.setSymbol(currency.getSymbol().toUpperCase());
        currency.setLastUpdated(LocalDateTime.now());
        return cryptoCurrencyRepository.save(currency);
    }

    // تحديث سعر العملة
    public CryptoCurrency updatePrice(String symbol, BigDecimal newPrice) {
        CryptoCurrency currency = cryptoCurrencyRepository.findById(symbol.toUpperCase())
                .orElseThrow(() -> new RuntimeException("Currency not found: " + symbol));

        // حساب التغيير
        BigDecimal oldPrice = currency.getCurrentPrice();
        if (oldPrice != null && oldPrice.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal change = newPrice.subtract(oldPrice);
            BigDecimal changePercentage = change.divide(oldPrice, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal(100));

            currency.setPriceChange24h(change.doubleValue());
            currency.setPriceChangePercentage24h(changePercentage.doubleValue());
        }

        currency.setCurrentPrice(newPrice);
        currency.setLastUpdated(LocalDateTime.now());

        return cryptoCurrencyRepository.save(currency);
    }

    // تفعيل/تعطيل المتابعة
    public CryptoCurrency toggleTracking(String symbol) {
        CryptoCurrency currency = cryptoCurrencyRepository.findById(symbol.toUpperCase())
                .orElseThrow(() -> new RuntimeException("Currency not found: " + symbol));

        currency.setTracking(!currency.isTracking());
        currency.setLastUpdated(LocalDateTime.now());

        return cryptoCurrencyRepository.save(currency);
    }

    // حذف عملة
    public void deleteCurrency(String symbol) {
        cryptoCurrencyRepository.deleteById(symbol.toUpperCase());
    }
}