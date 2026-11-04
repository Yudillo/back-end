package com.yudillo.api.auth.service;

import com.yudillo.api.auth.entity.EmailToken;
import com.yudillo.api.auth.entity.TokenPurpose;
import com.yudillo.api.auth.repository.EmailTokenRepository;
import com.yudillo.api.auth.repository.RefreshTokenRepository;
import com.yudillo.api.global.config.FrontendProperties;
import com.yudillo.api.global.exception.BusinessException;
import com.yudillo.api.global.exception.ErrorCode;
import com.yudillo.api.global.mail.EmailSender;
import com.yudillo.api.user.entity.User;
import com.yudillo.api.user.repository.UserRepository;
import java.security.SecureRandom;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int TOKEN_BYTES = 32;

    private final UserRepository userRepository;
    private final EmailTokenRepository emailTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailSender emailSender;
    private final FrontendProperties frontendProperties;

    @Transactional
    public void requestReset(String email) {
        userRepository.findByEmail(email).ifPresentOrElse(
                this::issueAndSend,
                () -> log.debug("password reset requested for unknown email")
        );
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        EmailToken emailToken = emailTokenRepository
                .findByTokenAndPurpose(token, TokenPurpose.PASSWORD_RESET)
                .filter(EmailToken::isUsable)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_EMAIL_TOKEN));

        User user = userRepository.findById(emailToken.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new BusinessException(ErrorCode.SAME_PASSWORD);
        }

        user.changePassword(passwordEncoder.encode(newPassword));
        emailToken.markUsed();
        refreshTokenRepository.deleteByUserId(user.getId());
    }

    private void issueAndSend(User user) {
        emailTokenRepository.deleteByUserIdAndPurposeAndUsedAtIsNull(user.getId(), TokenPurpose.PASSWORD_RESET);

        String token = generateToken();
        emailTokenRepository.save(EmailToken.issue(user.getId(), token, TokenPurpose.PASSWORD_RESET));

        String link = frontendProperties.baseUrl() + "/auth/reset-password?token=" + token;
        long minutes = TokenPurpose.PASSWORD_RESET.validity().toMinutes();
        emailSender.send(user.getEmail(), "[Yudillo] 비밀번호 재설정", """
                아래 링크에서 새 비밀번호를 설정하세요. (%d분간 유효)
                %s

                본인이 요청하지 않았다면 이 메일을 무시하세요.""".formatted(minutes, link));
    }

    /** 32바이트 난수 → URL 에 그대로 넣을 수 있는 Base64URL 문자열(43자). */
    private String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
