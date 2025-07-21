package com.synergyx.trading.util;
import com.synergyx.trading.enums.PeriodUnit;

/**
 * 패턴 등록 시 총 기간 유효성 검사를 수행합니다.
 */

public class PatternValidator {

    /**
     * 총 기간이 1일(1440분) 이상인지 검사합니다.
     *
     * @param unit   단위
     * @param value  값
     * @param length 패턴 길이 (points 개수)
     * @return 유효하면 true, 그렇지 않으면 false
     */

    public static boolean isValidDuration(PeriodUnit unit, int value, int length) {
        if (unit == null || length <= 0) {
            return false;
        }

        int totalMinutes;

        switch (unit) {
            case HOUR:
                if (value < 1 || value > 23) return false;
                totalMinutes = value * length * 60;
                break;
            case DAY:
                if (value < 1 || value > 30) return false;
                totalMinutes = value * length * 60 * 24;
                break;
            default:
                return false;
        }

        return totalMinutes >= 1440;
    }
}

