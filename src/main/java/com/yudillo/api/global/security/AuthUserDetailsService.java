package com.yudillo.api.global.security;

import com.yudillo.api.user.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .map(AuthUser::from)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));
    }

    /** JWT 의 subject(사용자 UUID)로 인증 주체를 복원한다. */
    @Transactional(readOnly = true)
    public AuthUser loadByUserId(UUID userId) {
        return userRepository.findById(userId)
                .map(AuthUser::from)
                .orElseThrow(() -> new JwtAuthenticationException("토큰에 해당하는 사용자가 없습니다."));
    }
}
