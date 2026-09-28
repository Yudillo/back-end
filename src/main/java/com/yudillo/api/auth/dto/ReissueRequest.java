package com.yudillo.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "ReissueRequest", description = "토큰 재발급 요청")
public record ReissueRequest(

        @Schema(description = "로그인 시 받은 refreshToken", example = "eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJ5dWRpbGxvLWFwaSIsInN1YiI6IjVkYzMxZTdhLTIxZDMtNGMwZi04ZjA1LWY5MzBlZjMwZDRkYiJ9.Xq7vN2kZ8pL4mR1sT6wY9uI3oA5bC0dE")
        @NotBlank(message = "refreshToken 을 입력하세요.")
        String refreshToken
) {
}
