package com.synergyx.trading.repository;

import com.synergyx.trading.model.KisToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KisTokenRepository extends JpaRepository<KisToken, Long> {
    Optional<KisToken> findTopByOrderByExpiresAtDesc();

    boolean existsByAccessToken(String accessToken);
}