package com.synergyx.trading.service.stockService.candle.strategy;

import com.synergyx.trading.dto.stockDetail.StockCandleResponseDTO;

import java.util.List;

public interface CandleCompressionStrategy {
    boolean supports(String interval);

    List<StockCandleResponseDTO> compress(Long stockId);
}