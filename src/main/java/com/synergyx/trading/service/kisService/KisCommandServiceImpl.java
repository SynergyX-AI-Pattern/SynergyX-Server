package com.synergyx.trading.service.kisService;

import com.synergyx.trading.client.KisClient;
import com.synergyx.trading.dto.kis.KisStockDTO;
import com.synergyx.trading.model.Stock;
import com.synergyx.trading.model.StockDetail;
import com.synergyx.trading.model.StockOhlcv;
import com.synergyx.trading.repository.StockDetailRepository;
import com.synergyx.trading.repository.StockOhlcvRepository;
import com.synergyx.trading.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KisCommandServiceImpl implements KisCommandService {
    private final KisClient kisClient;
    private final StockRepository stockRepository;
    private final StockDetailRepository stockDetailRepository;
    private final StockOhlcvRepository stockOhlcvRepository;

    @Transactional
    public void fetchAndSaveKospi100() {
        List<KisStockDTO> stockDtos = kisClient.getKospi100Stocks();

        for (KisStockDTO dto : stockDtos) {
            // 종목 저장 (upsert)
            Stock stock = stockRepository.findBySymbol(dto.getSymbol())
                    .orElseGet(() -> Stock.builder()
                            .symbol(dto.getSymbol())
                            .name(dto.getName())
                            .imageUrl("default.jpg")
                            .build());
            stockRepository.save(stock);

            // 현재가 저장 (update or insert)
            StockDetail detail = StockDetail.builder()
                    .stockId(stock.getId())
                    .stock(stock)
                    .price(dto.getPrice())
                    .financialData("{}") // 재무 데이터 미처리
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            stockDetailRepository.save(detail);

            // OHLCV 저장
            boolean exists = stockOhlcvRepository.existsByStockIdAndTimestamp(stock.getId(), dto.getTimestamp());
            if (!exists) {
                StockOhlcv ohlcv = StockOhlcv.builder()
                        .stock(stock)
                        .timestamp(dto.getTimestamp())
                        .open(dto.getOpen())
                        .high(dto.getHigh())
                        .low(dto.getLow())
                        .close(dto.getClose())
                        .volume(dto.getVolume())
                        .createdAt(LocalDateTime.now())
                        .build();
                stockOhlcvRepository.save(ohlcv);
            }
        }
    }
}
