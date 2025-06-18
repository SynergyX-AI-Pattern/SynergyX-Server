package com.synergyx.trading.service.stockService.candle;

import com.synergyx.trading.apiPayload.code.status.ErrorStatus;
import com.synergyx.trading.apiPayload.exception.GeneralException;
import com.synergyx.trading.dto.stockDetail.StockCandleResponseDTO;
import com.synergyx.trading.model.Stock;
import com.synergyx.trading.model.StockOhlcv;
import com.synergyx.trading.repository.StockOhlcvRepository;
import com.synergyx.trading.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockCandleQueryServiceImpl implements StockCandleQueryService {

    private final StockRepository stockRepository;
    private final StockOhlcvRepository stockOhlcvRepository;

    /**
     * 1일(12H) 분봉 캔들 데이터 조회
     *
     * @param stockId 종목 ID
     * @return 캔들 데이터 리스트
     */
    @Transactional(readOnly = true)
    public List<StockCandleResponseDTO> getDailyCandles(Long stockId) {
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.STOCK_NOT_FOUND));

        Pageable pageable = PageRequest.of(0, 27);
        List<StockOhlcv> candles = stockOhlcvRepository.findLastNCandlesByStock(stock.getId(), pageable); // 15분봉 기준 1day

        if (candles.isEmpty() || candles == null) {
            throw new GeneralException(ErrorStatus.CANDLE_DATA_NOT_FOUND);
        }

        Collections.reverse(candles);

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

