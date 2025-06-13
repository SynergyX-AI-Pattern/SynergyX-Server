package com.synergyx.trading.service.stockSearchService;

import com.synergyx.trading.dto.stockSearch.StockSearchResponseDTO;

import java.util.List;

public interface StockSearchQueryService {
    List<StockSearchResponseDTO> searchStocksByName(String keyword);
}
