package com.synergyx.trading.service.stockService.candle.strategy;

import com.synergyx.trading.apiPayload.code.status.ErrorStatus;
import com.synergyx.trading.apiPayload.exception.GeneralException;
import com.synergyx.trading.converter.StockCandleConverter;
import com.synergyx.trading.dto.stockDetail.StockCandleResponseDTO;
import com.synergyx.trading.enums.CandleInterval;
import com.synergyx.trading.model.StockOhlcv1d;
import com.synergyx.trading.repository.StockOhlcv1dRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * [3M] 3개월 캔들 전략
 * <p>
 * - interval = CandleInterval.THREE_MONTHS
 * - 최근 3개월 간의 일봉 데이터 조회 (63 (21*3) 개 고정)
 * - 시간 역순으로 조회 후, 시간순 정렬
 */
@Component
@RequiredArgsConstructor
public class ThreeMonthCompressionStrategy implements CandleCompressionStrategy {

    private final StockOhlcv1dRepository stockOhlcv1dRepository;
    private final StockCandleConverter stockCandleConverter;

    @Override
    public boolean supports(CandleInterval interval) {
        return interval == CandleInterval.THREE_MONTHS;
    }

    @Override
    public List<StockCandleResponseDTO> compress(Long stockId) {

        List<StockOhlcv1d> candles = stockOhlcv1dRepository.findTop63ByStockIdOrderByTimestampDesc(stockId);

        if (candles == null || candles.isEmpty()) {
            throw new GeneralException(ErrorStatus.CANDLE_DATA_NOT_FOUND);
        }

        Collections.reverse(candles);

        return stockCandleConverter.toDtoList(candles);
    }
}
