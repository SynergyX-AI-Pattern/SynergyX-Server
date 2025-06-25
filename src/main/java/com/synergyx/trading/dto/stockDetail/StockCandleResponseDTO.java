package com.synergyx.trading.dto.stockDetail;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockCandleResponseDTO {
    private LocalDateTime time;
    private Double open;
    private Double high;
    private Double low;
    private Double close;
    private Long volume;
}
