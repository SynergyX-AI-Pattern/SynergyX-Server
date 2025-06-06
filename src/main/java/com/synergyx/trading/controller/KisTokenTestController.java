package com.synergyx.trading.controller;

import com.synergyx.trading.service.kisService.KisTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/test/kis")
@Tag(name = "Test API", description = "테스트 API 입니다.")
public class KisTokenTestController {

    private final KisTokenService kisTokenService;

    @Operation(summary = "KIS Access Token API", description = "KIS의 Access Token 을 발급 또는 로드합니다.")
    @GetMapping("/token")
    public ResponseEntity<String> getAccessTokenForTest() {
        String accessToken = kisTokenService.getAccessToken();
        return ResponseEntity.ok("✅ 발급된 AccessToken: " + accessToken);
    }
}