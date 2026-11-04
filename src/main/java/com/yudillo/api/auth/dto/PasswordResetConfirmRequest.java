package com.yudillo.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(name = "PasswordResetConfirmRequest", description = "새 비밀번호 설정 요청")
public record PasswordResetConfirmRequest(

        @Schema(description = "재설정 메일 링크의 token 값", example = "q3Jx0pZ7mVd2LwH9sKfT1yNcR8bE4uAoG6iXjM5nQeU")
        @NotBlank(message = "token 을 입력하세요.")
        String token,

        @Schema(description = "새 비밀번호 (영문/숫자/특수문자 포함 8~20자)", example = "Yudillo!5678")
        @NotBlank(message = "새 비밀번호를 입력하세요.")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,20}$",
                message = "형식에 맞는 비밀번호를 입력하세요."
        )
        String newPassword
) {
}
