package com.synergyx.trading.util;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.RoundingMode;

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

    /**
     * 문자열을 Long 으로 파싱합니다.
     *
     * @param value 문자열
     * @return 변환된 Long 또는 null
     */
    public static Long toLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 천 단위를 (,)로 구분하여 변환합니다.
     *
     * @param value double, long, int
     * @return 문자열
     */
    public static String toFormattedNumber(double value) {
        return String.format("%,.0f", value);
    }

    public static String toFormattedNumber(long value) {
        return String.format("%,d", value);
    }

    public static String toFormattedNumber(int value) {
        return String.format("%,d", value);
    }

    /**
     * 문자열을 소수점 1자리 문자열로 변환합니다. (배)
     *
     * @param raw 문자열
     * @return 변환된 문자열 또는 null
     */
    public static String toFormattedRatio(String raw) {
        try {
            return String.format("%.1f배", Double.parseDouble(raw));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 문자열을 소수점 1,2자리 문자열로 변환합니다. (%)
     *
     * @param raw 문자열
     * @return 변환된 문자열 또는 null
     */
    public static String toFormattedPercentage(String raw, int digits) {
        try {
            double value = Double.parseDouble(raw);
            return String.format("%." + digits + "f%%", value);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Double 값을 퍼센트 포맷 문자열로 반환합니다.
     *
     * @param value
     * @param digits
     * @return 변환된 문자열
     */
    public static String toFormattedPercentage(Double value, int digits) {
        if (value == null) return "N/A";

        BigDecimal percent = BigDecimal.valueOf(value)
                .setScale(digits, RoundingMode.HALF_UP);

        return percent + "%";
    }

    /**
     * 억 단위 시가총액을 조 원 단위로 변환합니다.
     *
     * @param value 문자열
     * @return 변환된 문자열 또는 null
     */
    public static String toFormattedMarketCap(String value) {
        try {
            long raw = Long.parseLong(value); // 억 원 단위
            double trillion = raw / 10000.0;  // 조 원 단위로 변환
            return String.format("%.1f조원", trillion);
        } catch (Exception e) {
            return null;
        }
    }
}
