package com.synergyx.trading.service.stockService.ranking;

import com.synergyx.trading.dto.stockDetail.RankedStockDTO;

import java.util.List;

public interface StockRankingQueryService {
    List<RankedStockDTO> getTop20();

    List<RankedStockDTO> getAiTop20();
}
