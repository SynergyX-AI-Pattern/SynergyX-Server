package com.synergyx.trading.service.stockService.detail;

import com.synergyx.trading.dto.stockDetail.StockDetailResponseDTO;

public interface StockDetailQueryService {
    StockDetailResponseDTO getStockDetail(Long stockId, Long userId);
}
