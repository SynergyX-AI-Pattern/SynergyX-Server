package com.synergyx.trading.repository;

import com.synergyx.trading.model.KisToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KisTokenRepository extends JpaRepository<KisToken, Long> {
    KisToken findTopByOrderByUpdatedAtDesc();
}