package com.yudillo.api.user.dto;

import com.yudillo.api.user.entity.Role;
import com.yudillo.api.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(name = "UserResponse", description = "사용자 정보")
public record UserResponse(
        @Schema(description = "사용자 ID", example = "5dc31e7a-21d3-4c0f-8f05-f930ef30d4db")
        UUID id,
        @Schema(example = "user@example.com") String email,
        @Schema(example = "홍길동") String name,
        @Schema(description = "서비스 전역 권한", example = "USER")
        Role role,
        @Schema(description = "이메일 인증 완료 여부", example = "true")
        boolean emailVerified,
        @Schema(description = "가입 시각", example = "2026-09-28 11:45:11")
        Instant createdAt
) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole(),
                user.isEmailVerified(),
                user.getCreatedAt()
        );
    }
}
