package com.synergyx.trading.model;

import com.synergyx.trading.model.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "user")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 20)
    private String username;

    @Column(nullable = false, length = 255, unique = true)
    private String email;

    @Column(length = 255)
    private String image;

    @Column(nullable = false)
    private boolean agreeMarketing;

    @Column(nullable = false)
    private boolean agreeEvent;

    @Column(nullable = false)
    private boolean pushEnabled;

    @Column(columnDefinition = "TEXT")
    private String accessToken;

    @Column(columnDefinition = "TEXT")
    private String refreshToken;

    @Column(columnDefinition = "TEXT")
    private String fcmToken;
}
