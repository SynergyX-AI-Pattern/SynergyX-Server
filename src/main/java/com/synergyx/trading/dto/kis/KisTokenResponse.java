package com.synergyx.trading.dto.kis;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KisTokenResponse {
    private String access_token;
    private String token_type;
    private long expires_in;

    @JsonProperty("access_token_token_expired")
    private String accessTokenExpired;
}
