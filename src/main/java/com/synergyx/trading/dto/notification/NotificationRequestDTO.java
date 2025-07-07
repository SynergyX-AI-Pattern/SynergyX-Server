package com.synergyx.trading.dto.notification;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class NotificationRequestDTO {
    private String title;
    private String message;
    private String type;
}