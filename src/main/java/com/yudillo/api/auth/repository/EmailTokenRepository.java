package com.yudillo.api.auth.repository;

import com.yudillo.api.auth.entity.EmailToken;
import com.yudillo.api.auth.entity.TokenPurpose;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailTokenRepository extends JpaRepository<EmailToken, Long> {

    Optional<EmailToken> findByTokenAndPurpose(String token, TokenPurpose purpose);
    void deleteByUserIdAndPurposeAndUsedAtIsNull(UUID userId, TokenPurpose purpose);
}
