package com.synergyx.trading.service.stockDetailService;

import com.synergyx.trading.dto.stockDetail.StockDetailResponseDTO;

public interface StockDetailQueryService {
    StockDetailResponseDTO getStockDetail(Long stockId, Long userId);
}
