package com.yudillo.api.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

@Getter
@Entity
@Table(name = "email_tokens",
        indexes = @Index(name = "idx_email_tokens_user_id_purpose", columnList = "user_id, purpose"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "token", nullable = false, unique = true, length = 255)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", nullable = false, length = 20)
    private TokenPurpose purpose;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "used_at")
    private Instant usedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    private EmailToken(UUID userId, String token, TokenPurpose purpose) {
        Instant now = Instant.now();
        this.userId = userId;
        this.token = token;
        this.purpose = purpose;
        this.expiresAt = now.plus(purpose.validity());
        this.createdAt = now;
    }

    public static EmailToken issue(UUID userId, String token, TokenPurpose purpose) {
        return new EmailToken(userId, token, purpose);
    }

    public boolean isExpired() {
        return expiresAt.isBefore(Instant.now());
    }

    public boolean isUsed() {
        return usedAt != null;
    }

    public boolean isUsable() {
        return !isUsed() && !isExpired();
    }

    public void markUsed() {
        this.usedAt = Instant.now();
    }
}
