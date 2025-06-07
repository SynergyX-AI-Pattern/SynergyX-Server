package com.synergyx.trading.service.kisService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synergyx.trading.config.KisProperties;
import com.synergyx.trading.model.KisToken;
import com.synergyx.trading.repository.KisTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class KisTokenServiceImpl implements KisTokenService {

    private final KisTokenRepository kisTokenRepository;
    private final WebClient kisWebClient;
    private final ObjectMapper objectMapper;
    private final KisProperties kisProperties;

    /**
     * 유효한 Access token 이 있으면 반환, 없으면 새로 요청합니다.
     *
     * @return access token
     */
    @Override
    public String getAccessToken() {
        KisToken token = kisTokenRepository.findTopByOrderByUpdatedAtDesc();

        if (token != null && !isExpired(token)) {
            log.info("[KIS] DB에 저장된 유효한 AccessToken 사용");
            return token.getAccessToken();
        }

        log.info("[KIS] 토큰이 없거나 만료됨 → 새로 발급 요청");
        KisToken newToken = refreshAccessToken();
        return newToken.getAccessToken();
    }

    /**
     * 토큰이 만료되었는지 확인합니다.
     */
    private boolean isExpired(KisToken token) {
        return token.getExpiresAt().isBefore(LocalDateTime.now());
    }

    /**
     * 발급된 토큰을 파싱하여 DB에 저장합니다.
     */
    private KisToken refreshAccessToken() {
        String responseBody = requestNewToken();

        try {
            JsonNode json = objectMapper.readTree(responseBody);

            KisToken newToken = KisToken.builder()
                    .accessToken(json.get("access_token").asText())
                    .tokenType("Bearer")
                    .expiresAt(LocalDateTime.parse(
                            json.get("access_token_token_expired").asText(),
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    ))
                    .build();

            return kisTokenRepository.save(newToken);

        } catch (Exception e) {
            throw new RuntimeException("KIS 토큰 파싱 실패", e);
        }
    }

    /**
     * KIS 로부터 Access Token 을 받아옵니다.
     */
    private String requestNewToken() {
        return kisWebClient.post()
                .uri("/oauth2/tokenP")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(Map.of(
                        "grant_type", "client_credentials",
                        "appkey", kisProperties.getAppKey(),
                        "appsecret", kisProperties.getAppSecret()
                ))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}