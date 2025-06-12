package com.synergyx.trading.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConfigurationProperties(prefix = "kis")
@Getter
@Setter
public class KisProperties {
    private String appKey;
    private String appSecret;
    @PostConstruct
    public void printKeys() {
//        log.info("✅ KIS AppKey = {}", appKey);
//        log.info("✅ KIS AppSecret = {}", appSecret); // todo: delete
    }
}
