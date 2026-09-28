package com.yudillo.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "TokenResponse", description = "토큰 발급 결과")
public record TokenResponse(

        @Schema(description = "Access 토큰. Authorization: Bearer {accessToken} 으로 보낸다.",
                example = "eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJ5dWRpbGxvLWFwaSIsInN1YiI6IjVkYzMxZTdhLTIxZDMtNGMwZi04ZjA1LWY5MzBlZjMwZDRkYiJ9.Xq7vN2kZ8pL4mR1sT6wY9uI3oA5bC0dE")
        String accessToken,

        @Schema(description = "Refresh 토큰. Access 토큰 만료 시 /api/auth/reissue 로 재발급받는다.",
                example = "eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJ5dWRpbGxvLWFwaSIsInN1YiI6IjVkYzMxZTdhLTIxZDMtNGMwZi04ZjA1LWY5MzBlZjMwZDRkYiJ9.Xq7vN2kZ8pL4mR1sT6wY9uI3oA5bC0dE")
        String refreshToken,

        @Schema(description = "토큰 타입", example = "Bearer")
        String tokenType,

        @Schema(description = "Access 토큰 만료까지 남은 초", example = "1800")
        long expiresIn
) {

    public static TokenResponse of(String accessToken, String refreshToken, long expiresIn) {
        return new TokenResponse(accessToken, refreshToken, "Bearer", expiresIn);
    }
}
