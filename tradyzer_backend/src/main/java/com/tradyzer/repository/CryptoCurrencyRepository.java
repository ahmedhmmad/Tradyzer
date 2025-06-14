package com.tradyzer.repository;

import com.tradyzer.entity.CryptoCurrency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CryptoCurrencyRepository extends JpaRepository<CryptoCurrency, String> {

    List<CryptoCurrency> findByIsActiveTrue();

    List<CryptoCurrency> findByIsTrackingTrue();

    @Query("SELECT c FROM CryptoCurrency c WHERE c.isActive = true ORDER BY c.marketCap DESC")
    List<CryptoCurrency> findTopCryptocurrencies();
}