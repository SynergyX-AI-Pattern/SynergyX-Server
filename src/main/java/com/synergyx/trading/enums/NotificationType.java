package com.synergyx.trading.enums;

public enum NotificationType {
    BACKTEST_RESULT,
    PATTERN_DETECTED,
    SYSTEM;

    public static NotificationType fromCode(String code) {
        for (NotificationType type : values()) {
            if (type.name().equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("지원하지 않는 알림 유형입니다: " + code);
    }
}
