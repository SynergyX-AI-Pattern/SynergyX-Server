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
    public synchronized String getAccessToken() {
        KisToken token = kisTokenRepository.findTopByOrderByExpiresAtDesc().orElse(null);

        if (token != null && !isExpired(token)) {
            log.info("[KIS] DB 유효한 토큰 재사용 - expiresAt: {}", token.getExpiresAt());
            return token.getAccessToken();
        }

        // 토큰이 유효한지 재확인
        if (token != null && token.getExpiresAt().isAfter(LocalDateTime.now())) {
            log.warn("[KIS] expiresAt 유효하나 재발급 시도됨 -> 차단");
            return token.getAccessToken();
        }

        KisToken newToken = refreshAccessToken();
        log.info("[KIS] 새로운 토큰 발급 완료 - expiresAt={}", newToken.getExpiresAt());

        return newToken.getAccessToken();
    }

    /**
     * 토큰이 만료되었는지 확인합니다.
     */
    private boolean isExpired(KisToken token) {

        boolean expired = token.getExpiresAt().isBefore(LocalDateTime.now());
        log.debug("[KIS] 토큰 만료 확인: expiresAt={}, now={}, expired={}", token.getExpiresAt(), LocalDateTime.now(), expired);
        return expired;
    }

    /**
     * 발급된 토큰을 파싱하여 DB에 저장합니다.
     */
    private KisToken refreshAccessToken() {
        String responseBody = requestNewToken();
        log.debug("[KIS] 발급 응답: {}", responseBody);

        try {
            JsonNode json = objectMapper.readTree(responseBody);

            String accessToken = json.get("access_token").asText();

            // 동일 액세스 토큰을 응답받은 경우
            if (kisTokenRepository.existsByAccessToken(accessToken)) {
                log.warn("[KIS] 이미 발급된 토큰을 다시 응답 받음 → 저장 생략");
                return kisTokenRepository.findTopByOrderByExpiresAtDesc().orElse(null);
            }

            String expiresAtRaw = json.get("access_token_token_expired").asText();
            LocalDateTime expiresAt = LocalDateTime.parse(expiresAtRaw, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            KisToken newToken = KisToken.builder()
                    .accessToken(accessToken)
                    .tokenType("Bearer")
                    .expiresAt(expiresAt)
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