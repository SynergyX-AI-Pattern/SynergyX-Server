package com.synergyx.trading.dto.kis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KisTokenRequest {
    private String grant_type;
    private String appkey;
    private String appsecret;
}