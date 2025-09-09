package com.synergyx.trading.service.stockService.search;

import com.synergyx.trading.dto.stockSearch.StockSearchFastApiResponseDTO;
import com.synergyx.trading.apiPayload.code.status.ErrorStatus;
import com.synergyx.trading.apiPayload.exception.GeneralException;
import com.synergyx.trading.dto.stockSearch.StockSearchResponseDTO;
import com.synergyx.trading.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockSearchQueryServiceImpl implements StockSearchQueryService {

    private final StockRepository stockRepository;
    private final WebClient fastApiWebClient;

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

    /**
     * 이미지를 기반으로 종목을 추론하여 반환합니다.
     *
     * @param image 이미지 파일
     * @return StockSearchResponseDTO
     */
    @Override
    public StockSearchFastApiResponseDTO.StockSearchFastApiInfoResponseDTO searchStockByImage(MultipartFile image) {
        // 파일 검증
        if (image.isEmpty()) {
            throw new GeneralException(ErrorStatus.IMAGE_FILE_MISSING);
        }

        // 파일 크기 검증 (20MB 제한)
        if (image.getSize() > 20 * 1024 * 1024) {
            throw new GeneralException(ErrorStatus.IMAGE_FILE_TOO_LARGE);
        }

        // 파일 타입 검증
        String contentType = image.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new GeneralException(ErrorStatus.INVALID_IMAGE_FILE_TYPE);
        }

        try {
            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("image", image.getResource());

            StockSearchFastApiResponseDTO.StockSearchFastApiWrapperDTO response = fastApiWebClient.post()
                    .uri("/api/v1/stocks/search-by-image")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .bodyValue(builder.build())
                    .retrieve()
                    .bodyToMono(StockSearchFastApiResponseDTO.StockSearchFastApiWrapperDTO.class)
                    .block();

            if (response == null || !response.isSuccess()) {
                throw new GeneralException(ErrorStatus.IMAGE_STOCK_NOT_FOUND);
            }

            return StockSearchFastApiResponseDTO.StockSearchFastApiInfoResponseDTO.builder()
                    .id(response.getData().getId())
                    .name(response.getData().getName())
                    .imageUrl(response.getData().getImageUrl())
                    .status(response.getData().getStatus())
                    .build();

        } catch (Exception e) {
            throw new GeneralException(ErrorStatus.IMAGE_STOCK_NOT_FOUND);
        }
    }
}