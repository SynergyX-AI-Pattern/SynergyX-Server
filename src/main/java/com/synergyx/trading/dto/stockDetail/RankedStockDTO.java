package com.synergyx.trading.dto.stockDetail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RankedStockDTO {

    private Integer rank;
    private Long stockId;
    private String stockName;
    private String price;
    private String changeRate;
    private String imageUrl;
}

