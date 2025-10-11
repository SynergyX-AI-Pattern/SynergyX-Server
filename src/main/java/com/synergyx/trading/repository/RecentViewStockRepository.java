package com.synergyx.trading.repository;

import com.synergyx.trading.dto.InterestStock.InterestStockResponseDTO;
import com.synergyx.trading.model.RecentViewStock;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RecentViewStockRepository extends JpaRepository<RecentViewStock, Long> {

    @EntityGraph(attributePaths = {"stock", "stock.stockDetail"})
    List<RecentViewStock> findByUserIdOrderByViewedAtDesc(Long userId);

    // 중복 제거 (stockId 1개 삭제)
    void deleteByUserIdAndStockId(Long userId, Long stockId);

    // 20개 초과된 데이터 삭제
    @Modifying
    @Query(value = """
                DELETE r FROM recent_view_stock r
                WHERE r.user_id = :userId
                AND r.id NOT IN (
                    SELECT id
                    FROM (
                        SELECT id
                        FROM recent_view_stock
                        WHERE user_id = :userId
                        ORDER BY viewed_at DESC
                        LIMIT 20
                    ) tmp
                )
            """, nativeQuery = true)
    void deleteOverLimit(Long userId);
}
