package com.yudillo.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(name = "SignupRequest", description = "회원가입 요청")
public record SignupRequest(

        @Schema(description = "이메일", example = "user@example.com")
        @NotBlank(message = "이메일을 입력하세요.")
        @Email(message = "이메일 형식으로 입력하세요.")
        @Size(max = 255, message = "이메일이 너무 깁니다.")
        String email,

        @Schema(description = "비밀번호 (영문/숫자/특수문자 포함 8~20자)", example = "user123!")
        @NotBlank(message = "비밀번호를 입력하세요.")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,20}$",
                message = "형식에 맞는 비밀번호를 입력하세요."
        )
        String password,

        @Schema(description = "사용자 이름 (2~10자)", example = "홍길동")
        @NotBlank(message = "이름을 입력하세요.")
        @Pattern(
                regexp = "^[가-힣a-zA-Z0-9]{2,10}$",
                message = "형식에 맞는 이름을 입력하세요."
        )
        String name
) {
}
