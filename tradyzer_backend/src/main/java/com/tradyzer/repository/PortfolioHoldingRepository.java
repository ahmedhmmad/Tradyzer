package com.tradyzer.repository;

import com.tradyzer.entity.Portfolio;
import com.tradyzer.entity.PortfolioHolding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PortfolioHoldingRepository extends JpaRepository<PortfolioHolding, Long> {

    List<PortfolioHolding> findByPortfolio(Portfolio portfolio);

    Optional<PortfolioHolding> findByPortfolioAndSymbol(Portfolio portfolio, String symbol);
}