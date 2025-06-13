package com.synergyx.trading.service.stockSearchService;

import com.synergyx.trading.dto.stockSearch.StockSearchResponseDTO;
import com.synergyx.trading.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockSearchQueryServiceImpl implements StockSearchQueryService {

    private final StockRepository stockRepository;

    /**
     * 종목명을 기준으로 종목 리스트를 검색합니다.
     *
     * @param keyword 검색어
     * @return StockSearchResponseDTO list
     */
    @Override
    public List<StockSearchResponseDTO> searchStocksByName(String keyword) {
        return stockRepository.findByNameContaining(keyword).stream()
                .map(stock -> StockSearchResponseDTO.builder()
                        .id(stock.getId())
                        .name(stock.getName())
                        .imageUrl(stock.getImageUrl())
                        .build())
                .toList();
    }
}
