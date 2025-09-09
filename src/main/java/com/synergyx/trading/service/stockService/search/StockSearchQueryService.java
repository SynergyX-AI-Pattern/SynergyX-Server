package com.synergyx.trading.service.stockService.search;

import com.synergyx.trading.dto.stockSearch.StockSearchFastApiResponseDTO;
import com.synergyx.trading.dto.stockSearch.StockSearchResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface StockSearchQueryService {
    List<StockSearchResponseDTO> searchStocksByName(String keyword);

    // 이미지 기반 검색
    StockSearchFastApiResponseDTO.StockSearchFastApiInfoResponseDTO searchStockByImage(MultipartFile image);
}
