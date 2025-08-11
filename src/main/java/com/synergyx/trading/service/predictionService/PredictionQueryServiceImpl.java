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
        LocalDate endDate = addBizDays(startDate, 13); // 시작일 포함 총 14영업일

        log.debug("[PredictionQueryService] Prediction window startDate={}, endDate={}", startDate, endDate);

        // 예측가 집계 조회
        PredictionWindowAggProjection agg = predictionRepository.aggregateWindow(stockId, startDate, endDate)
                .orElseThrow(() -> {
                    log.warn("[PredictionQueryService] No prediction data found for stockId={}, start={}, end={}",
                            stockId, startDate, endDate);
                    return new GeneralException(ErrorStatus.STOCK_PREDICTION_NOT_FOUND);
                });

        double upper = agg.getUpperForecast();
        double lower = agg.getLowerForecast();

        log.debug("[PredictionQueryService] Upper forecast={}, Lower forecast={}", upper, lower);

        // 버퍼 적용 + 틱 단위 조정
        double fairSell = roundDown(upper * (1 - BUFFER_PCT), tickFor(upper));
        double fairBuy = roundUp(lower * (1 + BUFFER_PCT), tickFor(lower));

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
                .expiresAt(asOfDate.plusDays(1).atTime(9, 0))
                .build();

        log.debug("[PredictionQueryService] Returning DTO={}", dto);

        return dto;
    }

    private LocalDate getNextBizDay(LocalDate date) {
        LocalDate next = date.plusDays(1);
        while (isWeekend(next)) {
            next = next.plusDays(1);
        }
        return next;
    }

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
     * 가격대에 맞는 호가 단위를 반환합니다.
     * 구간별 최소 호가 단위 기준입니다.
     * <p>
     * - 4,800원 → 1원 단위
     * - 9,500원 → 5원 단위
     * - 42,000원 → 10원 단위
     * - 87,000원 → 50원 단위
     * - 120,000원 → 100원 단위
     *
     * @param price 적용할 가격
     * @return 호가 단위
     */
    private double tickFor(double price) {
        if (price < 5000) return 1;
        if (price < 10000) return 5;
        if (price < 50000) return 10;
        if (price < 100000) return 50;
        return 100;
    }

    /**
     * 가격을 호가 단위에 맞춰 내림합니다.
     * - 매도가 계산 시 사용
     * - 해당 호가 단위의 가장 가까운 값으로 조정
     * <p>
     * ex:
     * - value=62,622, tick=50 → 62,600
     * - value=9,987, tick=5 → 9,985
     *
     * @param value
     * @param tick
     * @return
     */
    private double roundDown(double value, double tick) {
        return Math.floor(value / tick) * tick;
    }

    /**
     * 가격을 호가 단위에 맞춰 올림합니다.
     * - 매수가 계산 시 사용
     * - 해당 호가 단위의 가장 가까운 높은 값으로 조정
     * <p>
     * ex:
     * - value=53,418, tick=100 → 53,500
     * - value=9,987, tick=5 → 9,990
     *
     * @param value
     * @param tick
     * @return
     */
    private double roundUp(double value, double tick) {
        return Math.ceil(value / tick) * tick;
    }
}