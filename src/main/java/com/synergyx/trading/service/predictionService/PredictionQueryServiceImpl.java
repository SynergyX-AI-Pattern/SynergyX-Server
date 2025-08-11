package com.synergyx.trading.service.predictionService;

import com.synergyx.trading.apiPayload.code.status.ErrorStatus;
import com.synergyx.trading.apiPayload.exception.GeneralException;
import com.synergyx.trading.dto.stockDetail.PredictionWindowSummaryDTO;
import com.synergyx.trading.repository.PredictionRepository;
import com.synergyx.trading.repository.PredictionWindowAggProjection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class PredictionQueryServiceImpl implements PredictionQueryService {

    private final PredictionRepository predictionRepository;

    private static final int WINDOW_DAYS = 14; // 예측 윈도우
    private static final double BUFFER_PCT = 0.006; // 버퍼

    /**
     * 특정 종목의 예측 상/하한가 적정 매수/매도 가를 계산합니다.
     *
     * @param stockId
     * @param asOfDate 오늘 날짜
     * @return PredictionWindowSummaryDTO 계산 전용 dto
     */
    @Override
    @Transactional(readOnly = true)
    public PredictionWindowSummaryDTO getWindowSummary(Long stockId, LocalDate asOfDate) {

        // 예측 윈도우 계산
        LocalDate startDate = getNextBizDay(asOfDate);
        LocalDate endDate = addBizDays(startDate, WINDOW_DAYS - 1); // 시작일 포함 총 14영업일

        log.debug("[PredictionQueryService] Prediction window startDate={}, endDate={}", startDate, endDate);

        // 예측가 집계 조회
        PredictionWindowAggProjection agg = predictionRepository.aggregateWindow(stockId, startDate, endDate)
                .orElseThrow(() -> {
                    log.warn("[PredictionQueryService] No prediction data found for stockId={}, start={}, end={}",
                            stockId, startDate, endDate);
                    return new GeneralException(ErrorStatus.STOCK_PREDICTION_NOT_FOUND);
                });

        double rawUpper = agg.getUpperForecast();
        double rawLower = agg.getLowerForecast();

        // 예측값 유효성 검사
        if (!Double.isFinite(rawUpper) || !Double.isFinite(rawLower)) {
            log.warn("[PredictionQueryService] Invalid forecasts: upper={}, lower={}", rawUpper, rawLower);
            throw new GeneralException(ErrorStatus.STOCK_PREDICTION_NOT_FOUND);
        }

        // 상/하한 역전 방어
        if (rawUpper < rawLower) {
            log.warn("[PredictionQueryService] upper < lower (upper={}, lower={}) - swapping", rawUpper, rawLower);
            double tmp = rawUpper;
            rawUpper = rawLower;
            rawLower = tmp;
        }

        // 반올림 적용
        long upper = Math.round(rawUpper);
        long lower = Math.round(rawLower);
        log.debug("[PredictionQueryService] Upper forecast={}, Lower forecast={}", upper, lower);

        // 버퍼 적용 + 틱 단위 조정
        long bufferedUpper = Math.round(upper * (1 - BUFFER_PCT));
        long bufferedLower = Math.round(lower * (1 + BUFFER_PCT));
        long fairSell = roundDown(bufferedUpper, tickFor(bufferedUpper));
        long fairBuy = roundUp(bufferedLower, tickFor(bufferedLower));

        // 매도/매수 역전 방어
        if (fairBuy > fairSell) {
            log.warn("[PredictionQueryService] fairBuy({}) > fairSell({}) - adjusting to midpoint", fairBuy, fairSell);
            long mid = Math.round((upper + lower) / 2.0);
            long tick = tickFor(mid);
            fairBuy = roundDown(mid, tick);
            fairSell = roundUp(mid, tick);
        }

        log.debug("[PredictionQueryService] Fair sell(after buffer/tick)={}, Fair buy(after buffer/tick)={}",
                fairSell, fairBuy);

        PredictionWindowSummaryDTO dto = PredictionWindowSummaryDTO.builder()
                .stockId(stockId)
                .asOfDate(asOfDate)
                .windowDays(WINDOW_DAYS)
                .upperForecast(upper)
                .lowerForecast(lower)
                .fairSell(fairSell)
                .fairBuy(fairBuy)
                .bufferPct(BUFFER_PCT)
                .build();

        log.debug("[PredictionQueryService] Returning DTO={}", dto);

        return dto;
    }

    /**
     * 다음 영업일 계산
     */
    private LocalDate getNextBizDay(LocalDate date) {
        LocalDate next = date.plusDays(1);
        while (isWeekend(next)) {
            next = next.plusDays(1);
        }
        return next;
    }

    /**
     * 영업일 기준 days일 뒤 날짜 계산
     */
    private LocalDate addBizDays(LocalDate start, int days) {
        LocalDate date = start;
        int added = 0;
        while (added < days) {
            date = date.plusDays(1);
            if (!isWeekend(date)) {
                added++;
            }
        }
        return date;
    }

    private boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.SATURDAY ||
                date.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    /**
     * 가격대에 맞는 호가 단위를 반환합니다. (코스피 기준)
     * 구간별 최소 호가 단위 기준입니다.
     * <p>
     * ex:
     * - 1,500원  → 1원 단위
     * - 3,000원  → 5원 단위
     * - 8,000원  → 10원 단위
     * - 15,000원 → 10원 단위
     * - 42,000원 → 50원 단위
     * - 87,000원 → 100원 단위
     * - 350,000원 → 500원 단위
     * - 600,000원 → 1,000원 단위
     *
     * @param price 적용할 가격
     * @return 호가 단위
     */
    private long tickFor(double price) {
        if (price < 2000) return 1;
        else if (price < 5000) return 5;
        else if (price < 20000) return 10;
        else if (price < 50000) return 50;
        else if (price < 200000) return 100;
        else if (price < 500000) return 500;
        return 1000;
    }

    /**
     * 가격을 호가 단위에 맞춰 내림합니다.
     * - 매도가 계산 시 사용
     * - 해당 호가 단위의 가장 가까운 값으로 조정
     * <p>
     * ex:
     * - value=62,622, tick=50 → 62,600
     * - value=9,987, tick=10 → 9,980
     *
     * @param value
     * @param tick
     * @return
     */
    private long roundDown(long value, long tick) {
        return (value / tick) * tick;
    }

    /**
     * 가격을 호가 단위에 맞춰 올림합니다.
     * - 매수가 계산 시 사용
     * - 해당 호가 단위의 가장 가까운 높은 값으로 조정
     * <p>
     * ex:
     * - value=53,418, tick=100 → 53,500
     * - value=9,987, tick=10 → 9,990
     *
     * @param value
     * @param tick
     * @return
     */
    private long roundUp(long value, long tick) {
        return ((value + tick - 1) / tick) * tick;
    }
}