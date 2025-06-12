package com.synergyx.trading.model;

import com.synergyx.trading.model.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Table(name = "pattern_apply", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"stock_id", "pattern_id"})
})
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatternApply extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 패턴 테이블 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pattern_id",  nullable = false)
    private Pattern pattern;

    // 종목 테이블 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    // 사용자 테이블 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 알림 설정 여부
    @Column(name = "is_alert_enabled", nullable = false)
    private Boolean isAlertEnabled;

}

