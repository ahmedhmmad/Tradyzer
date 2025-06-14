package com.tradyzer.repository;

import com.tradyzer.entity.Portfolio;
import com.tradyzer.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    List<Portfolio> findByUserAndIsActiveTrue(User user);

    Optional<Portfolio> findByUserAndIsDefaultTrue(User user);

    boolean existsByUserAndName(User user, String name);
}