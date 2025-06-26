package com.synergyx.trading.service.InterestStockService;

import com.synergyx.trading.dto.InterestStock.InterestStockResponseDTO;

import java.util.List;

public interface InterestStockQueryService {
    List<InterestStockResponseDTO> getInterestList(Long userId);

    List<InterestStockResponseDTO> getRecentViewStocks(Long userId);
}
