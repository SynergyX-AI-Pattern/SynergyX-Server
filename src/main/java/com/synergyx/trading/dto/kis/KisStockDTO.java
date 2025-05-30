package com.synergyx.trading.dto.kis;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class KisStockDTO {
    private String symbol;    // 종목코드
    private String name;      // 종목명
    private Float price;      // 현재가
    private Float open;
    private Float high;
    private Float low;
    private Float close;
    private Integer volume;
    private LocalDateTime timestamp;
}
