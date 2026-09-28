package com.yudillo.api.auth.service;

import com.yudillo.api.auth.dto.LoginRequest;
import com.yudillo.api.auth.dto.SignupRequest;
import com.yudillo.api.auth.dto.TokenResponse;
import com.yudillo.api.auth.entity.RefreshToken;
import com.yudillo.api.auth.repository.RefreshTokenRepository;
import com.yudillo.api.global.exception.BusinessException;
import com.yudillo.api.global.exception.ErrorCode;
import com.yudillo.api.global.security.JwtTokenProvider;
import com.yudillo.api.global.security.TokenType;
import com.yudillo.api.user.dto.UserResponse;
import com.yudillo.api.user.entity.Role;
import com.yudillo.api.user.entity.User;
import com.yudillo.api.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 회원가입 / 로그인 / 토큰 재발급 / 로그아웃.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Transactional
    public UserResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .name(request.name())
                .role(Role.USER)
                // TODO 이메일 인증 메일 발송을 붙이면 false 로 두고 인증 후 verifyEmail() 호출
                .emailVerified(true)
                .build();

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        return issueTokens(user);
    }

    /** Refresh 토큰으로 Access/Refresh 를 새로 발급한다(rotation). */
    @Transactional
    public TokenResponse reissue(String refreshToken) {
        Claims claims = tokenProvider.parse(refreshToken, TokenType.REFRESH);
        UUID userId = tokenProvider.getUserId(claims);

        RefreshToken saved = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));

        if (saved.isExpired() || !saved.getUserId().equals(userId)) {
            refreshTokenRepository.delete(saved);
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String newAccessToken = tokenProvider.createAccessToken(user);
        String newRefreshToken = tokenProvider.createRefreshToken(user);
        saved.rotate(newRefreshToken,
                tokenProvider.getExpiresAt(tokenProvider.parse(newRefreshToken, TokenType.REFRESH)));

        return TokenResponse.of(newAccessToken, newRefreshToken, tokenProvider.accessTokenValiditySeconds());
    }

    /** 저장된 Refresh 토큰을 모두 지워 재발급을 막는다. Access 토큰은 만료까지 유효. */
    @Transactional
    public void logout(UUID userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    private TokenResponse issueTokens(User user) {
        String accessToken = tokenProvider.createAccessToken(user);
        String refreshToken = tokenProvider.createRefreshToken(user);

        refreshTokenRepository.deleteByUserId(user.getId());
        refreshTokenRepository.save(RefreshToken.issue(
                user.getId(),
                refreshToken,
                tokenProvider.getExpiresAt(tokenProvider.parse(refreshToken, TokenType.REFRESH))
        ));

        return TokenResponse.of(accessToken, refreshToken, tokenProvider.accessTokenValiditySeconds());
    }
}
