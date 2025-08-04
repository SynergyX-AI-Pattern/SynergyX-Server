package com.synergyx.trading.service.stockService.candle.strategy;

import com.synergyx.trading.apiPayload.code.status.ErrorStatus;
import com.synergyx.trading.apiPayload.exception.GeneralException;
import com.synergyx.trading.converter.StockCandleConverter;
import com.synergyx.trading.dto.stockDetail.StockCandleResponseDTO;
import com.synergyx.trading.enums.CandleInterval;
import com.synergyx.trading.model.StockOhlcv1m;
import com.synergyx.trading.repository.StockOhlcv1mRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * [1Y] 1년 캔들 전략
 * <p>
 * - interval = CandleInterval.ONE_YEAR
 * - 최근 12개월 간의 월봉 데이터 조회 (12개 고정)
 * - 시간 역순으로 조회 후, 시간순 정렬
 */
@Component
@RequiredArgsConstructor
public class OneYearCompressionStrategy implements CandleCompressionStrategy {

    private final StockOhlcv1mRepository stockOhlcv1mRepository;
    private final StockCandleConverter stockCandleConverter;

    @Override
    public boolean supports(CandleInterval interval) {
        return interval == CandleInterval.ONE_YEAR;
    }

    @Override
    public List<StockCandleResponseDTO> compress(Long stockId) {

        List<StockOhlcv1m> candles = stockOhlcv1mRepository.findTop12ByStockIdOrderByTimestampDesc(stockId);

        if (candles == null || candles.isEmpty()) {
            throw new GeneralException(ErrorStatus.CANDLE_DATA_NOT_FOUND);
        }

        Collections.reverse(candles);

        return stockCandleConverter.toDtoList(candles);
    }
}
