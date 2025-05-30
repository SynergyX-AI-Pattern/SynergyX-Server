package com.synergyx.trading.config;

import com.querydsl.core.annotations.Config;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "kis")
@Getter
@Setter
public class KisProperties {
    private String appKey;
    private String appSecret;
    private String accessToken;
}
