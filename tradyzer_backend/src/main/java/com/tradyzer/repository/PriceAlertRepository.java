package com.tradyzer.repository;

import com.tradyzer.entity.PriceAlert;
import com.tradyzer.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PriceAlertRepository extends JpaRepository<PriceAlert, Long> {

    List<PriceAlert> findByUser(User user);

    List<PriceAlert> findByUserAndIsActiveTrue(User user);

    List<PriceAlert> findByIsActiveTrue();

    List<PriceAlert> findBySymbolAndIsActiveTrue(String symbol);
}