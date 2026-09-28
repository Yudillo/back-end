package com.yudillo.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "LoginRequest", description = "로그인 요청")
public record LoginRequest(

        @Schema(description = "이메일", example = "user@example.com")
        @NotBlank(message = "이메일을 입력하세요.")
        @Email(message = "이메일 형식으로 입력하세요.")
        String email,

        @Schema(description = "비밀번호", example = "user123!")
        @NotBlank(message = "비밀번호를 입력하세요.")
        String password
) {
}
