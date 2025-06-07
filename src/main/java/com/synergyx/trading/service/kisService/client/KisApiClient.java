package com.synergyx.trading.service.kisService.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synergyx.trading.config.KisProperties;
import com.synergyx.trading.service.kisService.KisTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class KisApiClient {

    private final WebClient kisWebClient;
    private final KisProperties kisProperties;
    private final KisTokenService tokenService;
    private final ObjectMapper objectMapper;

    /**
     * 공통 GET 요청을 처리합니다.
     */
    public JsonNode get(String path, Map<String, String> queryParams, String trId) {
        try {
            String token = tokenService.getAccessToken();

            WebClient.RequestHeadersSpec<?> request = kisWebClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path(path);
                        queryParams.forEach(uriBuilder::queryParam);
                        return uriBuilder.build();
                    })
                    .headers(headers -> {
                        headers.setBearerAuth(token);
                        headers.set("appkey", kisProperties.getAppKey());
                        headers.set("appsecret", kisProperties.getAppSecret());
                        headers.set("tr_id", trId);
                        headers.set("custtype", "P");
                    });

            String response = request.retrieve().bodyToMono(String.class).block();
            return objectMapper.readTree(response);

        } catch (Exception e) {
            throw new RuntimeException("[KIS API 호출 실패] " + path + ": " + e.getMessage(), e);
        }
    }
}
