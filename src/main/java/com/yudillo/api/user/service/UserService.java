package com.yudillo.api.user.service;

import com.yudillo.api.auth.repository.RefreshTokenRepository;
import com.yudillo.api.global.exception.BusinessException;
import com.yudillo.api.global.exception.ErrorCode;
import com.yudillo.api.user.dto.ChangePasswordRequest;
import com.yudillo.api.user.dto.UpdateNameRequest;
import com.yudillo.api.user.dto.UserResponse;
import com.yudillo.api.user.entity.User;
import com.yudillo.api.user.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserResponse getMe(UUID userId) {
        return UserResponse.from(findUser(userId));
    }

    @Transactional
    public UserResponse updateName(UUID userId, UpdateNameRequest request) {
        User user = findUser(userId);
        // 이름은 식별자가 아니라 표시용이므로 중복을 허용한다. (식별자는 email)
        user.changeName(request.name());
        return UserResponse.from(user);
    }

    /** 비밀번호를 바꾸면 기존 Refresh 토큰을 모두 폐기해 다른 기기 세션을 끊는다. */
    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = findUser(userId);

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_MISMATCH);
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.SAME_PASSWORD);
        }

        user.changePassword(passwordEncoder.encode(request.newPassword()));
        refreshTokenRepository.deleteByUserId(userId);
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
