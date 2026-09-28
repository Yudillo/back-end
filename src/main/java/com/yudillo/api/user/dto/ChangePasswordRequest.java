package com.yudillo.api.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(name = "ChangePasswordRequest", description = "비밀번호 변경 요청")
public record ChangePasswordRequest(

        @Schema(description = "현재 비밀번호", example = "user123")
        @NotBlank(message = "현재 비밀번호를 입력하세요.")
        String currentPassword,

        @Schema(description = "새 비밀번호 (영문/숫자/특수문자 포함 8~20자)", example = "Yudillo!5678")
        @NotBlank(message = "새 비밀번호를 입력하세요.")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,20}$",
                message = "형식에 맞는 비밀번호를 입력하세요."
        )
        String newPassword
) {
}
