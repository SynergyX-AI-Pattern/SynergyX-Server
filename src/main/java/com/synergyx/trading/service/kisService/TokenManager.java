package com.synergyx.trading.service.kisService;

import com.synergyx.trading.client.KisClient;
import com.synergyx.trading.dto.kis.KisTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class TokenManager {

    private final KisClient kisClient;

    private String accessToken;
    private LocalDateTime expiredAt;

    public String getKisAccessToken() {
        if (accessToken == null || expiredAt.isBefore(LocalDateTime.now())) {
            refreshToken();
        }
        return accessToken;
    }

    private void refreshToken() {
        KisTokenResponse response = kisClient.fetchToken();

        this.accessToken = response.getAccess_token();

        this.expiredAt = LocalDateTime.parse(
                response.getAccessTokenExpired(),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        );
    }
}
