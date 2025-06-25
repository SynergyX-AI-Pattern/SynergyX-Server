package com.synergyx.trading.service.stockService.candle;

import com.synergyx.trading.apiPayload.code.status.ErrorStatus;
import com.synergyx.trading.apiPayload.exception.GeneralException;
import com.synergyx.trading.dto.stockDetail.StockCandleResponseDTO;
import com.synergyx.trading.enums.CandleInterval;
import com.synergyx.trading.service.stockService.candle.strategy.CandleCompressionStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

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
     * @param stockId      종목 ID
     * @param intervalCode 캔들 구간 ("1D", "1W", "3M", "1Y", "5Y")
     * @return 캔들 응답 DTO 리스트
     */
    @Transactional(readOnly = true)
    public List<StockCandleResponseDTO> getCandles(Long stockId, String intervalCode) {
        CandleInterval interval = CandleInterval.fromCode(intervalCode);

        return strategies.stream()
                .filter(strategy -> strategy.supports(interval))
                .findFirst()
                .orElseThrow(() -> new GeneralException(ErrorStatus.INVALID_CANDLE_INTERVAL))
                .compress(stockId);
    }
}

