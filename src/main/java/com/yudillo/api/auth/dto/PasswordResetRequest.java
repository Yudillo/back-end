package com.yudillo.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "PasswordResetRequest", description = "비밀번호 재설정 메일 요청")
public record PasswordResetRequest(

        @Schema(description = "가입한 이메일", example = "user@example.com")
        @NotBlank(message = "이메일을 입력하세요.")
        @Email(message = "이메일 형식으로 입력하세요.")
        @Size(max = 255, message = "이메일이 너무 깁니다.")
        String email
) {
}
