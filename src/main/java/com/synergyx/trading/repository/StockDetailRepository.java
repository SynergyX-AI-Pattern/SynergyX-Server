package com.synergyx.trading.repository;

import com.synergyx.trading.model.StockDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockDetailRepository extends JpaRepository<StockDetail, Long> {
    Optional<StockDetail> findByStock_Symbol(String symbol);

    Optional<StockDetail> findByStock_Id(Long stockId);
}