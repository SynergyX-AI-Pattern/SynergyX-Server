package com.synergyx.trading.dto.stockSearch;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockSearchResponseDTO {

    private Long id; // stock id
    private String name;
    private String imageUrl;
}