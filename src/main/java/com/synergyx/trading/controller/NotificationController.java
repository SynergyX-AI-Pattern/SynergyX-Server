package com.synergyx.trading.controller;

import com.synergyx.trading.apiPayload.ApiResponse;
import com.synergyx.trading.config.context.UserContext;
import com.synergyx.trading.dto.notification.NotificationRequestDTO;
import com.synergyx.trading.dto.notification.NotificationResponseDTO;
import com.synergyx.trading.service.notificationService.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.synergyx.trading.apiPayload.code.status.SuccessStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("test/notifications")
@Tag(name = "알림 API", description = "알림 관련 API 입니다.")
public class NotificationController {

    private final UserContext userContext;
    private final NotificationService notificationService;

    @Operation(summary = "알림 푸시 및 저장",
            description = "FCM 푸시 알림을 전송하고, 알림 내용을 db에 저장합니다."
    )
    @PostMapping("/send")
    public ResponseEntity<?> sendAndSaveNotification(
            @RequestBody @Valid NotificationRequestDTO request
    ) {
        Long userId = request.getUserId();
        NotificationResponseDTO result = notificationService.sendAndSaveNotification(userId, request);

        return ResponseEntity.ok(ApiResponse.onSuccess(
                result,
                SuccessStatus.SUCCESS_NOTIFICATION_SENT.getCode(),
                SuccessStatus.SUCCESS_NOTIFICATION_SENT.getMessage()
        ));
    }
}

