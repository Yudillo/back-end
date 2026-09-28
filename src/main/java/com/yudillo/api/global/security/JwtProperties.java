package com.yudillo.api.global.security;

import jakarta.validation.constraints.NotBlank;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * application.yml 의 jwt.* 설정. secret 은 반드시 환경변수로 주입한다.
 */
@Validated
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        @NotBlank String secret,
        String issuer,
        Duration accessTokenValidity,
        Duration refreshTokenValidity
) {

    public JwtProperties {
        issuer = (issuer == null || issuer.isBlank()) ? "yudillo-api" : issuer;
        accessTokenValidity = accessTokenValidity == null ? Duration.ofMinutes(30) : accessTokenValidity;
        refreshTokenValidity = refreshTokenValidity == null ? Duration.ofDays(14) : refreshTokenValidity;
    }
}
