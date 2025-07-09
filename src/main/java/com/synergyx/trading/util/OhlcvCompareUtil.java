package com.synergyx.trading.util;

import com.synergyx.trading.model.StockOhlcv1d;
import com.synergyx.trading.model.StockOhlcv1h;
import com.synergyx.trading.model.StockOhlcv1m;

import java.util.Objects;

public class OhlcvCompareUtil {

    /**
     * 기존 OHLCV 데이터와 새로운 값들을 비교하여 변경 여부를 판단합니다.
     *
     * @param existing 기존 저장된 OHLCV
     * @param open     새로 수집한 시가
     * @param high     새로 수집한 고가
     * @param low      새로 수집한 저가
     * @param close    새로 수집한 종가
     * @param volume   새로 수집한 거래량
     * @return 값이 다르면 true (즉, 업데이트가 필요함)
     */
    public static boolean isDifferent(
            StockOhlcv1h existing,
            Double open,
            Double high,
            Double low,
            Double close,
            Long volume
    ) {
        return !Objects.equals(existing.getOpen(), open)
                || !Objects.equals(existing.getHigh(), high)
                || !Objects.equals(existing.getLow(), low)
                || !Objects.equals(existing.getClose(), close)
                || !Objects.equals(existing.getVolume(), volume);
    }

    public static boolean isDifferent(
            StockOhlcv1d existing,
            Double open,
            Double high,
            Double low,
            Double close,
            Long volume
    ) {
        return !Objects.equals(existing.getOpen(), open)
                || !Objects.equals(existing.getHigh(), high)
                || !Objects.equals(existing.getLow(), low)
                || !Objects.equals(existing.getClose(), close)
                || !Objects.equals(existing.getVolume(), volume);
    }

    public static boolean isDifferent(
            StockOhlcv1m existing,
            Double open,
            Double high,
            Double low,
            Double close,
            Long volume
    ) {
        return !Objects.equals(existing.getOpen(), open)
                || !Objects.equals(existing.getHigh(), high)
                || !Objects.equals(existing.getLow(), low)
                || !Objects.equals(existing.getClose(), close)
                || !Objects.equals(existing.getVolume(), volume);
    }
}
