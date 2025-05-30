package com.synergyx.trading.repository;

import com.synergyx.trading.model.StockDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockDetailRepository extends JpaRepository<StockDetail, Integer> {
}