package com.tradyzer.repository;

import com.tradyzer.entity.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {

    List<PriceHistory> findBySymbolOrderByRecordedAtDesc(String symbol);

    @Query("SELECT p FROM PriceHistory p WHERE p.symbol = :symbol AND p.recordedAt >= :startTime ORDER BY p.recordedAt DESC")
    List<PriceHistory> findBySymbolAndTimeRange(@Param("symbol") String symbol, @Param("startTime") LocalDateTime startTime);

    @Query("SELECT p FROM PriceHistory p WHERE p.symbol = :symbol ORDER BY p.recordedAt DESC LIMIT 1")
    Optional<PriceHistory> findLatestBySymbol(@Param("symbol") String symbol);

    @Query("SELECT DISTINCT p.symbol FROM PriceHistory p")
    List<String> findDistinctSymbols();
}