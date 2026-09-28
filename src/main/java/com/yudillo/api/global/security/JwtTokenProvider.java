package com.yudillo.api.global.security;

import com.yudillo.api.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Access / Refresh 토큰 발급과 검증을 담당. HS256 대칭키를 사용한다.
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private static final String CLAIM_TYPE = "type";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_EMAIL = "email";

    private final JwtProperties properties;
    private final SecretKey key;

    public JwtTokenProvider(JwtProperties properties) {
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(properties.issuer())
                .subject(user.getId().toString())
                .claim(CLAIM_TYPE, TokenType.ACCESS.name())
                .claim(CLAIM_ROLE, user.getRole().name())
                .claim(CLAIM_EMAIL, user.getEmail())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(properties.accessTokenValidity())))
                .signWith(key)
                .compact();
    }

    public String createRefreshToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(properties.issuer())
                .subject(user.getId().toString())
                .id(UUID.randomUUID().toString())
                .claim(CLAIM_TYPE, TokenType.REFRESH.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(properties.refreshTokenValidity())))
                .signWith(key)
                .compact();
    }

    /** 서명과 만료를 검증하고 클레임을 돌려준다. 실패 시 JwtAuthenticationException. */
    public Claims parse(String token, TokenType expectedType) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .requireIssuer(properties.issuer())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            if (!expectedType.name().equals(claims.get(CLAIM_TYPE, String.class))) {
                throw new JwtAuthenticationException("토큰 종류가 올바르지 않습니다.");
            }
            return claims;
        } catch (ExpiredJwtException e) {
            throw new JwtAuthenticationException("만료된 토큰입니다.", e);
        } catch (JwtException | IllegalArgumentException e) {
            throw new JwtAuthenticationException("유효하지 않은 토큰입니다.", e);
        }
    }

    public UUID getUserId(Claims claims) {
        return UUID.fromString(claims.getSubject());
    }

    public Instant getExpiresAt(Claims claims) {
        return claims.getExpiration().toInstant();
    }

    public long accessTokenValiditySeconds() {
        return properties.accessTokenValidity().toSeconds();
    }
}
