package com.synergyx.trading.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ParsingUtil {

    /**
     * 문자열을 Double 로 파싱합니다.
     *
     * @param value
     * @return 변환된 Double 또는 null
     */
    public static Double toDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return null;
        }
    }
}
