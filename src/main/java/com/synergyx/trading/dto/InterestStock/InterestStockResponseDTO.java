package com.synergyx.trading.dto.InterestStock;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterestStockResponseDTO {
    private Long stockId;
    private String stockName;
    private String stockSymbol;
    private String imageUrl;
}
