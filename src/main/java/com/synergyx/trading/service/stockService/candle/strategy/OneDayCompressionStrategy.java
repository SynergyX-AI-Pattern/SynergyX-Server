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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * [1D] 하루 캔들 전략
 * <p>
 * - interval = CandleInterval.ONE_DAY
 * - 최근 1일치의 15분봉 데이터를 조회 (27개 고정)
 * - 시간 역순으로 조회 후, 시간순 정렬
 * - 1일치 15분봉 리스트 반환
 */
@Component
@RequiredArgsConstructor
public class OneDayCompressionStrategy implements CandleCompressionStrategy {

    private final StockRepository stockRepository;
    private final StockOhlcvRepository stockOhlcvRepository;

    @Override
    public boolean supports(CandleInterval interval) {
        return interval == CandleInterval.ONE_DAY;
    }

    @Override
    public List<StockCandleResponseDTO> compress(Long stockId) {
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.STOCK_NOT_FOUND));

        Pageable pageable = PageRequest.of(0, 27); // 약 1일치 15분봉
        List<StockOhlcv> candles = stockOhlcvRepository.findLastNCandlesByStock(stock.getId(), pageable);

        if (candles == null || candles.isEmpty()) {
            throw new GeneralException(ErrorStatus.CANDLE_DATA_NOT_FOUND);
        }

        Collections.reverse(candles); // 시간순 정렬

        return candles.stream()
                .map(candle -> StockCandleResponseDTO.builder()
                        .time(candle.getTimestamp())
                        .open(candle.getOpen())
                        .high(candle.getHigh())
                        .low(candle.getLow())
                        .close(candle.getClose())
                        .volume(candle.getVolume())
                        .build())
                .filter(dto -> Objects.nonNull(dto.getOpen()) && Objects.nonNull(dto.getClose()))
                .collect(Collectors.toList());
    }
}