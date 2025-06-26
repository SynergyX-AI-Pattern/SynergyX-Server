package com.synergyx.trading.dto.InterestStock;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Builder
public class InterestStockResponseDTO {
    private Long stockId;
    private String stockName;
    private String stockSymbol;
    private String imageUrl;

    public InterestStockResponseDTO(Long stockId, String stockName, String stockSymbol, String imageUrl) {
        this.stockId = stockId;
        this.stockName = stockName;
        this.stockSymbol = stockSymbol;
        this.imageUrl = imageUrl;
    }
}
