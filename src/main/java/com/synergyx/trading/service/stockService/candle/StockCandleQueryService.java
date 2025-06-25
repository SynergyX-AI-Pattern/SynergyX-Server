package com.synergyx.trading.service.stockService.candle;

import com.synergyx.trading.dto.stockDetail.StockCandleResponseDTO;

import java.util.List;

public interface StockCandleQueryService {

    // 캔들 데이터 조회
    List<StockCandleResponseDTO> getCandles(Long stockId, String interval);
}
