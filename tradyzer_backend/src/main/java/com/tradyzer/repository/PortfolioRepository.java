package com.tradyzer.repository;

import com.tradyzer.entity.Portfolio;
import com.tradyzer.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    List<Portfolio> findByUserAndIsActiveTrue(User user);

    Optional<Portfolio> findByUserAndIsDefaultTrue(User user);

    @Query("SELECT p FROM Portfolio p LEFT JOIN FETCH p.holdings WHERE p.id = :id")
    Optional<Portfolio> findByIdWithHoldings(Long id);

    boolean existsByUserAndName(User user, String name);
}