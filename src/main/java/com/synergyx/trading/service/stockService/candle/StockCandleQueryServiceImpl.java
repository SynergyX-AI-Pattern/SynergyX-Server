package com.synergyx.trading.service.stockService.candle;

import com.synergyx.trading.apiPayload.code.status.ErrorStatus;
import com.synergyx.trading.apiPayload.exception.GeneralException;
import com.synergyx.trading.converter.StockCandleConverter;
import com.synergyx.trading.dto.stockDetail.StockCandleResponseDTO;
import com.synergyx.trading.enums.CandleInterval;
import com.synergyx.trading.model.StockOhlcv;
import com.synergyx.trading.repository.StockOhlcv1dRepository;
import com.synergyx.trading.repository.StockOhlcvRepository;
import com.synergyx.trading.service.stockService.candle.strategy.CandleCompressionStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.TreeMap;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockCandleQueryServiceImpl implements StockCandleQueryService {

    private final List<CandleCompressionStrategy> strategies;

    /**
     * 캔들 데이터를 조회합니다.
     * <p>
     * interval 에 따라 압축 전략을 적용합니다.
     * CandleCompressionStrategy 인터페이스 기반으로 동적 매칭됩니다.
     *
     * @param stockId     종목 ID
     * @param intervalStr 캔들 구간 ("1D", "1W", "3M", "1Y", "5Y")
     * @return 캔들 응답 DTO 리스트
     */
    @Transactional(readOnly = true)
    public List<StockCandleResponseDTO> getCandles(Long stockId, String intervalStr) {

        String effectiveInterval = intervalStr.toUpperCase();

        CandleInterval interval = CandleInterval.fromCode(effectiveInterval);

        return strategies.stream()
                .filter(strategy -> strategy.supports(interval))
                .findFirst()
                .orElseThrow(() -> new GeneralException(ErrorStatus.INVALID_CANDLE_INTERVAL))
                .compress(stockId);
    }

    private final StockOhlcvRepository stockOhlcvRepository;
    private final StockOhlcv1dRepository stockOhlcv1dRepository;
    private final StockCandleConverter stockCandleConverter;

    /**
     * 백테스트 결과 차트용 일봉 데이터를 조회합니다.
     * <p>
     * startDate ~ endDate 구간의 캔들 데이터를 반환합니다.
     *
     * @param stockId   종목 ID
     * @param startDate 시작일 (LocalDate)
     * @param endDate   종료일 (LocalDate)
     * @return 캔들 응답 DTO 리스트
     */
    @Override
    @Transactional(readOnly = true)
    public List<StockCandleResponseDTO> getBacktestDailyCandles(Long stockId, LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new GeneralException(ErrorStatus._BAD_REQUEST);
        }

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        var candles = stockOhlcv1dRepository
                .findByStockIdAndTimestampBetweenOrderByTimestampAsc(stockId, start, end);

        if (candles == null || candles.isEmpty()) {
            throw new GeneralException(ErrorStatus.CANDLE_DATA_NOT_FOUND);
        }

        return stockCandleConverter.toDtoList(candles);
    }

    /**
     * 백테스트 결과 차트용 시간봉 데이터를 조회합니다.
     * <p>
     * startDate ~ endDate 구간의 캔들 데이터를 반환합니다.
     *
     * @param stockId    종목 ID
     * @param startDate  시작일시 (LocalDateTime)
     * @param endDate    종료일시 (LocalDateTime)
     * @return 캔들 응답 DTO 리스트
     */
    @Override
    @Transactional(readOnly = true)
    public List<StockCandleResponseDTO> getBacktestHourlyCandles(Long stockId, LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            throw new GeneralException(ErrorStatus._BAD_REQUEST);
        }

        // 15분봉 데이터 조회
        var candles15m = stockOhlcvRepository
                .findByStockIdAndTimestampBetweenOrderByTimestampAsc(stockId, startDate, endDate);

        if (candles15m == null || candles15m.isEmpty()) {
            throw new GeneralException(ErrorStatus.CANDLE_DATA_NOT_FOUND);
        }


        // 15분봉 -> 1시간봉 리샘플링
        Map<LocalDateTime, List<StockOhlcv>> grouped = candles15m.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getTimestamp().truncatedTo(java.time.temporal.ChronoUnit.HOURS),
                        TreeMap::new,
                        Collectors.toList()
                ));

        return grouped.entrySet().stream()
                .map(entry -> {
                    List<StockOhlcv> group = entry.getValue();
                    group.sort(java.util.Comparator.comparing(StockOhlcv::getTimestamp));
                    return StockCandleResponseDTO.builder()
                            .time(entry.getKey())
                            .open(group.get(0).getOpen())
                            .high(group.stream().mapToDouble(StockOhlcv::getHigh).max().orElse(0))
                            .low(group.stream().mapToDouble(StockOhlcv::getLow).min().orElse(0))
                            .close(group.get(group.size() - 1).getClose())
                            .volume(group.stream().mapToLong(StockOhlcv::getVolume).sum())
                            .build();
                })
                .toList();
    }
}