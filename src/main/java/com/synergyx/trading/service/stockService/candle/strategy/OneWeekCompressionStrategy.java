package com.synergyx.trading.service.stockService.candle.strategy;

import com.synergyx.trading.apiPayload.code.status.ErrorStatus;
import com.synergyx.trading.apiPayload.exception.GeneralException;
import com.synergyx.trading.dto.stockDetail.StockCandleResponseDTO;
import com.synergyx.trading.enums.CandleInterval;
import com.synergyx.trading.model.Stock;
import com.synergyx.trading.model.StockOhlcv;
import com.synergyx.trading.repository.StockOhlcvRepository;
import com.synergyx.trading.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * [1주] 15분봉 데이터를 1시간 단위로 압축하는 전략
 * <p>
 * - interval = CandleInterval.ONE_WEEK
 * - 최근 7일간의 15분봉 데이터를 조회하여, 동일한 시각(정각 기준)끼리 그룹핑
 * - 각 그룹을 1시간봉 하나로 압축
 * (open: 첫 봉, close: 마지막 봉, high/low: 최대/최소, volume: 합산)
 * - 하루 약 6개 캔들 반환, 총 약 40~50개 반환
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OneWeekCompressionStrategy implements CandleCompressionStrategy {

    private final StockRepository stockRepository;
    private final StockOhlcvRepository stockOhlcvRepository;

    @Override
    public boolean supports(CandleInterval interval) {
        return interval == CandleInterval.ONE_WEEK;
    }

    @Override
    public List<StockCandleResponseDTO> compress(Long stockId) {
        log.info("[1W] 주간 캔들 데이터 압축 시작: stockId={}, interval=1W", stockId);

        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.STOCK_NOT_FOUND));

        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<StockOhlcv> rawCandles = stockOhlcvRepository
                .findByStockIdAndTimestampAfter(stock.getId(), sevenDaysAgo);

        if (rawCandles == null || rawCandles.isEmpty()) {
            log.warn("[1W] 캔들 데이터 없음: stockId={}", stockId);
            throw new GeneralException(ErrorStatus.CANDLE_DATA_NOT_FOUND);
        }

        // 1시간 단위로 그룹핑: 15분봉 → 1시간봉 압축
        Map<LocalDateTime, List<StockOhlcv>> grouped = rawCandles.stream()
                .collect(Collectors.groupingBy(c -> truncateToHour(c.getTimestamp())));

        return grouped.entrySet().stream()
                .map(entry -> compressGroup(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(StockCandleResponseDTO::getTime))
                .collect(Collectors.toList());
    }

    /**
     * 15분봉 → 1시간봉 압축
     */
    private StockCandleResponseDTO compressGroup(LocalDateTime time, List<StockOhlcv> group) {
        group.sort(Comparator.comparing(StockOhlcv::getTimestamp));

        Double open = group.get(0).getOpen();
        Double close = group.get(group.size() - 1).getClose();
        Double high = group.stream().mapToDouble(StockOhlcv::getHigh).max().orElse(0);
        Double low = group.stream().mapToDouble(StockOhlcv::getLow).min().orElse(0);
        Long volume = group.stream().mapToLong(StockOhlcv::getVolume).sum();

        return StockCandleResponseDTO.builder()
                .time(time)
                .open(open)
                .close(close)
                .high(high)
                .low(low)
                .volume(volume)
                .build();
    }

    /**
     * LocalDateTime → 해당 시각의 정각 (분, 초 제거)
     */
    private LocalDateTime truncateToHour(LocalDateTime dateTime) {
        return dateTime.truncatedTo(ChronoUnit.HOURS);
    }
}
