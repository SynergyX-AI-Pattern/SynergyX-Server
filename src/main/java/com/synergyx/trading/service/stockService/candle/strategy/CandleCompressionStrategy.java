package com.synergyx.trading.service.stockService.candle.strategy;

import com.synergyx.trading.dto.stockDetail.StockCandleResponseDTO;
import com.synergyx.trading.enums.CandleInterval;

import java.util.List;

public interface CandleCompressionStrategy {
    boolean supports(CandleInterval interval);

    List<StockCandleResponseDTO> compress(Long stockId);
}