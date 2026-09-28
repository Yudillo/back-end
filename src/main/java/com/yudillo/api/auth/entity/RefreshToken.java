package com.yudillo.api.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 발급한 Refresh 토큰을 저장한다. 로그아웃/재발급 시 서버가 토큰을 무효화할 수 있게 하는 용도.
 */
@Getter
@Entity
@Table(name = "refresh_tokens", indexes = @Index(name = "idx_refresh_tokens_user_id", columnList = "user_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "token", nullable = false, unique = true, length = 512)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    private RefreshToken(UUID userId, String token, Instant expiresAt) {
        this.userId = userId;
        this.token = token;
        this.expiresAt = expiresAt;
        this.createdAt = Instant.now();
    }

    public static RefreshToken issue(UUID userId, String token, Instant expiresAt) {
        return new RefreshToken(userId, token, expiresAt);
    }

    public boolean isExpired() {
        return expiresAt.isBefore(Instant.now());
    }

    public void rotate(String token, Instant expiresAt) {
        this.token = token;
        this.expiresAt = expiresAt;
    }
}
