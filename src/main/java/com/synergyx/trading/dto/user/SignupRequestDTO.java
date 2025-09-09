package com.synergyx.trading.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "회원가입 요청 DTO")
public record SignupRequestDTO(

        @NotBlank(message = "이름은 필수입니다.")
        @Schema(description = "사용자 이름", example = "홍길동")
        String name,

        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        @Schema(description = "사용자 이메일", example = "example@email.com")
        String email,

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해야 합니다.")
        @Schema(description = "사용자 비밀번호", example = "aw3afAe@")
        String password,

        @Schema(description = "마케팅 활용 동의 여부", example = "true")
        boolean marketing,

        @Schema(description = "이벤트 알림 수신 동의 여부", example = "false")
        boolean event
) {
}
