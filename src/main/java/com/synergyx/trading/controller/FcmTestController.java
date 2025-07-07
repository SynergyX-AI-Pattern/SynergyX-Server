package com.synergyx.trading.controller;

import com.synergyx.trading.apiPayload.ApiResponse;
import com.synergyx.trading.service.fcmService.FCMService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/test/fcm")
@Tag(name = "FCM Test API", description = "FCM 테스트 API입니다.")
public class FcmTestController {

    private final FCMService fcmService;

    @Operation(summary = "FCM 푸시 알림 테스트 API", description = "Firebase로 푸시 알림을 전송합니다.")
    @PostMapping("/push")
    public ResponseEntity<?> sendTestFcmPush(
            @Parameter(description = "FCM 토큰", required = true)
            @RequestParam String token) {
        try {
            fcmService.sendPushNotification(token, "테스트 알림", "Spring Boot에서 보낸 테스트 알림입니다.");
            return ResponseEntity.ok(ApiResponse.onSuccess("FCM_SEND_SUCCESS", "푸시 알림 전송 완료"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.onFailure("FCM_SEND_FAILED", "푸시 알림 전송 실패", e.getMessage()));
        }
    }
}