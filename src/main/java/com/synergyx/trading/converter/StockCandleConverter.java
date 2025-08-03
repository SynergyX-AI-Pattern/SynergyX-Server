package com.synergyx.trading.converter;

import com.synergyx.trading.dto.stockDetail.StockCandleResponseDTO;
import com.synergyx.trading.model.common.Ohlcv;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class StockCandleConverter {

    public List<StockCandleResponseDTO> toDtoList(List<? extends Ohlcv> ohlcvs) {
        return ohlcvs.stream()
                .map(o -> StockCandleResponseDTO.builder()
                        .time(o.getTimestamp())
                        .open(o.getOpen())
                        .close(o.getClose())
                        .high(o.getHigh())
                        .low(o.getLow())
                        .volume(o.getVolume())
                        .build())
                .collect(Collectors.toList());
    }
}
