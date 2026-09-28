package com.yudillo.api.auth.controller;

import com.yudillo.api.auth.dto.LoginRequest;
import com.yudillo.api.auth.dto.ReissueRequest;
import com.yudillo.api.auth.dto.SignupRequest;
import com.yudillo.api.auth.dto.TokenResponse;
import com.yudillo.api.auth.service.AuthService;
import com.yudillo.api.global.config.ApiErrorCodes;
import com.yudillo.api.global.exception.ErrorCode;
import com.yudillo.api.global.config.ApiSuccessMessage;
import com.yudillo.api.global.response.ApiResponse;
import com.yudillo.api.global.response.SuccessMessage;
import com.yudillo.api.global.security.AuthUser;
import com.yudillo.api.global.security.CurrentUser;
import com.yudillo.api.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "회원가입 / 로그인 / 토큰")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @ApiErrorCodes({ErrorCode.INVALID_INPUT, ErrorCode.DUPLICATE_EMAIL})
    @ApiSuccessMessage(SuccessMessage.SIGNUP)
    @Operation(summary = "회원가입")
    @SecurityRequirements
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ApiResponse.ok(authService.signup(request), SuccessMessage.SIGNUP);
    }

    @ApiErrorCodes({ErrorCode.INVALID_INPUT, ErrorCode.INVALID_CREDENTIALS})
    @ApiSuccessMessage(SuccessMessage.LOGIN)
    @Operation(summary = "로그인", description = "Access / Refresh 토큰을 발급한다.")
    @SecurityRequirements
    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request), SuccessMessage.LOGIN);
    }

    @ApiErrorCodes({ErrorCode.INVALID_INPUT, ErrorCode.INVALID_TOKEN,
                    ErrorCode.EXPIRED_TOKEN, ErrorCode.USER_NOT_FOUND})
    @Operation(summary = "토큰 재발급", description = "Refresh 토큰으로 Access / Refresh 를 다시 발급한다.")
    @SecurityRequirements
    @PostMapping("/reissue")
    public ApiResponse<TokenResponse> reissue(@Valid @RequestBody ReissueRequest request) {
        return ApiResponse.ok(authService.reissue(request.refreshToken()));
    }

    @ApiSuccessMessage(SuccessMessage.LOGOUT)
    @Operation(summary = "로그아웃", description = "서버에 저장된 Refresh 토큰을 폐기한다.")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@CurrentUser AuthUser authUser) {
        authService.logout(authUser.getId());
        return ApiResponse.ok(SuccessMessage.LOGOUT);
    }
}
