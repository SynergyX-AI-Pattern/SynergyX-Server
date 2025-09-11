package com.synergyx.trading.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "프로필 수정 요청 DTO")
public record ProfileUpdateRequestDTO(

        @NotBlank(message = "이름은 필수입니다.")
        @Schema(description = "새로운 이름", example = "아무개")
        String name
) {
}
