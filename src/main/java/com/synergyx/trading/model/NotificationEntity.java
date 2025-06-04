package com.synergyx.trading.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 사용자 구현 후 매핑
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id", nullable = false)
//    private UserEntity user;

    @Column(name = "title", nullable = false)
    private String title; // 제목

    @Column(name = "message", nullable = false)
    private String message; // 메시지

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false; // 읽음 여부

    @Column(name = "is_valid", nullable = false)
    private Boolean isValid = true; // 삭제 여부

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt; // 생성 일시
}
