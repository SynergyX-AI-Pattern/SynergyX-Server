package com.synergyx.trading.dto.notification;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class NotificationResponseDTO {
    private Long id;
    private String title;
    private String message;
    private Boolean isRead;
    private Boolean isValid;
    private String type;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime createdAt;
}
