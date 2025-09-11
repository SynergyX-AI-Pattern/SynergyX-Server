package com.synergyx.trading.controller;

import com.synergyx.trading.apiPayload.ApiResponse;
import com.synergyx.trading.apiPayload.code.status.SuccessStatus;
import com.synergyx.trading.config.context.UserContext;
import com.synergyx.trading.dto.user.ProfileUpdateRequestDTO;
import com.synergyx.trading.dto.user.UserRequestDTO;
import com.synergyx.trading.service.userService.UserCommandService;
import com.synergyx.trading.service.userService.UserQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
@Tag(name = "사용자 API", description = "사용자 관련 API 입니다.")
public class UserController {

    private final UserContext userContext;
    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;

    @Operation(summary = "FCM 토큰 저장", description = "사용자의 FCM 토큰을 저장합니다.")
    @PatchMapping("/fcm-token")
    public ResponseEntity<?> updateFcmToken(
            @RequestBody UserRequestDTO.UpdateFcmTokenRequest request
    ) {
        Long userId = userContext.getCurrentUserId();
        userCommandService.updateFcmToken(userId, request.getFcmToken());
        return ResponseEntity.ok(
                ApiResponse.onSuccess(
                        SuccessStatus.SUCCESS_FCM_TOKEN_UPDATED.getCode(),
                        SuccessStatus.SUCCESS_FCM_TOKEN_UPDATED.getMessage()
                )
        );
    }

    @GetMapping("/profile")
    @Operation(summary = "프로필 조회",
            description = "사용자의 프로필을 조회합니다.")
    public ResponseEntity<ApiResponse<String>> getProfile(
    ) {
        Long userId = userContext.getCurrentUserId();

        String name = userQueryService.getProfileName(userId);

        return ResponseEntity.ok(ApiResponse.onSuccess(
                name,
                SuccessStatus.USER_PROFILE_SUCCESS.getCode(),
                SuccessStatus.USER_PROFILE_SUCCESS.getMessage()
        ));
    }

    @PatchMapping("/profile")
    @Operation(summary = "프로필 수정",
            description = "사용자의 프로필을 수정합니다.")
    public ResponseEntity<ApiResponse<Void>> updateProfile(
            @Valid @RequestBody ProfileUpdateRequestDTO requestDTO
    ) {
        Long userId = userContext.getCurrentUserId();

        userCommandService.updateProfile(userId, requestDTO);

        return ResponseEntity.ok(ApiResponse.onSuccess(
                SuccessStatus.USER_PROFILE_UPDATE_SUCCESS.getCode(),
                SuccessStatus.USER_PROFILE_UPDATE_SUCCESS.getMessage()
        ));
    }
}