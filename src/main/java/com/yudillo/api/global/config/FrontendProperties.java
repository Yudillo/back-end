package com.yudillo.api.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** 메일 링크에 들어가는 프론트엔드 주소. */
@ConfigurationProperties(prefix = "app.frontend")
public record FrontendProperties(String baseUrl) {

    public FrontendProperties {
        baseUrl = (baseUrl == null || baseUrl.isBlank()) ? "http://localhost:3000" : baseUrl;
    }
}
