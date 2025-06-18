package com.synergyx.trading.service.stockService.candle;

import com.synergyx.trading.dto.stockDetail.StockCandleResponseDTO;

import java.util.List;

public interface StockCandleQueryService {

    // 1일 캔들 조회
    List<StockCandleResponseDTO> getDailyCandles(Long stockId);
}
