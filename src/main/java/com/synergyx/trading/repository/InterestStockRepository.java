package com.synergyx.trading.repository;

import com.synergyx.trading.model.InterestStock;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterestStockRepository extends JpaRepository<InterestStock, Long> {
    Optional<InterestStock> findByUserIdAndStockId(Long userId, Long stockId);

    @EntityGraph(attributePaths = {"stock", "stock.stockDetail"})
    List<InterestStock> findAllByUserId(Long userId);

    boolean existsByUserIdAndStockId(Long userId, Long stockId);
}

