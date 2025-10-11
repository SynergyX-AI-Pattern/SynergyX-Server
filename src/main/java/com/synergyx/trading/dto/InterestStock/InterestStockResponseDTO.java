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
    private String price;
    private String changeRate;
    private String imageUrl;

    public InterestStockResponseDTO(Long stockId, String stockName, String stockSymbol, String price, String changeRate, String imageUrl) {
        this.stockId = stockId;
        this.stockName = stockName;
        this.stockSymbol = stockSymbol;
        this.price = price;
        this.changeRate = changeRate;
        this.imageUrl = imageUrl;
    }
}
