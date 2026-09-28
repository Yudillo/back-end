package com.yudillo.api.global.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 프론트엔드 오리진 허용 목록. 로컬은 http://localhost:3000 (vite dev 서버).
 */
@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(List<String> allowedOrigins) {

    public CorsProperties {
        allowedOrigins = (allowedOrigins == null || allowedOrigins.isEmpty())
                ? List.of("http://localhost:3000")
                : allowedOrigins;
    }
}
