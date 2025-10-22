package com.synergyx.trading.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 숫자 관련 공통 유틸리티 클래스
 */
public class NumberUtil {

    /**
     * 값을 소수점 둘째 자리까지 반올림합니다.
     *
     * @param value Double 값
     * @return 소수점 둘째 자리까지 반올림된 값
     */
    public static Double round(Double value) {
        if (value == null) return null;
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)  // 둘째 자리에서 반올림
                .doubleValue();
    }
}
