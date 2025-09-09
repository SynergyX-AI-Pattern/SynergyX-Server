package com.synergyx.trading.controller;

import com.synergyx.trading.apiPayload.ApiResponse;
import com.synergyx.trading.apiPayload.code.status.SuccessStatus;
import com.synergyx.trading.config.context.UserContext;
import com.synergyx.trading.dto.user.LoginRequestDTO;
import com.synergyx.trading.dto.user.LoginResponseDTO;
import com.synergyx.trading.dto.user.SignupRequestDTO;
import com.synergyx.trading.service.userService.UserAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "인증 API", description = "인증 API 입니다.")
public class AuthController {

    private final UserContext userContext;
    private final UserAuthService userAuthService;

    @PostMapping("/signup")
    @Operation(summary = "회원가입",
            description = "새로운 계정을 생성합니다.")
    public ResponseEntity<ApiResponse<Void>> signup(
            @Valid @RequestBody SignupRequestDTO request
    ) {
        userAuthService.createUser(request);

        return ResponseEntity.ok(ApiResponse.onSuccess(
                SuccessStatus.AUTH_SIGNUP_SUCCESS.getCode(),
                SuccessStatus.AUTH_SIGNUP_SUCCESS.getMessage()
        ));
    }

    @PostMapping("/login")
    @Operation(summary = "로그인",
            description = "생성된 계정에 로그인하여 JWT를 발급받는 API입니다.")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(
            @Valid @RequestBody LoginRequestDTO request
    ) {
        String email = request.email();
        String password = request.password();

        LoginResponseDTO loginResponse = userAuthService.login(email, password);

        return ResponseEntity.ok(ApiResponse.onSuccess(
                loginResponse,
                SuccessStatus.AUTH_LOGIN_SUCCESS.getCode(),
                SuccessStatus.AUTH_LOGIN_SUCCESS.getMessage()
        ));
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃",
            description = "현재 로그인된 사용자의 JWT를 무효화하는 API입니다.")
    public ResponseEntity<ApiResponse<Void>> logout(
    ) {
        Long userId = userContext.getCurrentUserId();

        userAuthService.logout(userId);

        return ResponseEntity.ok(ApiResponse.onSuccess(
                SuccessStatus.AUTH_LOGOUT_SUCCESS.getCode(),
                SuccessStatus.AUTH_LOGOUT_SUCCESS.getMessage()
        ));
    }
}