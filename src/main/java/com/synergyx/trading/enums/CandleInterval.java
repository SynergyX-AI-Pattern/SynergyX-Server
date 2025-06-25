package com.synergyx.trading.enums;

import java.time.LocalDateTime;

public enum CandleInterval {

    ONE_DAY("1D") {
        @Override
        public LocalDateTime getFromTime() {
            return LocalDateTime.now().minusDays(1);
        }
    },
    ONE_WEEK("1W") {
        @Override
        public LocalDateTime getFromTime() {
            return LocalDateTime.now().minusWeeks(1);
        }
    },
    THREE_MONTHS("3M") {
        @Override
        public LocalDateTime getFromTime() {
            return LocalDateTime.now().minusMonths(3);
        }
    },
    ONE_YEAR("1Y") {
        @Override
        public LocalDateTime getFromTime() {
            return LocalDateTime.now().minusYears(1);
        }
    },
    FIVE_YEARS("5Y") {
        @Override
        public LocalDateTime getFromTime() {
            return LocalDateTime.now().minusYears(5);
        }
    };

    private final String code;

    CandleInterval(String code) {
        this.code = code;
    }

    public abstract LocalDateTime getFromTime();

    public String getCode() {
        return code;
    }

    public static CandleInterval fromCode(String code) {
        for (CandleInterval interval : values()) {
            if (interval.code.equalsIgnoreCase(code)) {
                return interval;
            }
        }
        throw new IllegalArgumentException("지원하지 않는 interval입니다: " + code);
    }
}
