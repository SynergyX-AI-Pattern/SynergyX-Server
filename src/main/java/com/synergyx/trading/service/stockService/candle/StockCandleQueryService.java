package com.synergyx.trading.service.stockService.candle;

import com.synergyx.trading.dto.stockDetail.StockCandleResponseDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface StockCandleQueryService {

    // 캔들 데이터 조회
    List<StockCandleResponseDTO> getCandles(Long stockId, String interval);

    // 백테스팅 결과 전용 캔들 데이터 조회
    // 일봉
    List<StockCandleResponseDTO> getBacktestDailyCandles(Long stockId, LocalDate startDate, LocalDate endDate);
    // 시간봉
    List<StockCandleResponseDTO> getBacktestHourlyCandles(Long stockId, LocalDateTime startDate, LocalDateTime endDate);
}
