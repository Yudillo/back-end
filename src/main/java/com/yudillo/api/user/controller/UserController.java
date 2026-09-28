package com.yudillo.api.user.controller;

import com.yudillo.api.global.config.ApiErrorCodes;
import com.yudillo.api.global.exception.ErrorCode;
import com.yudillo.api.global.config.ApiSuccessMessage;
import com.yudillo.api.global.response.ApiResponse;
import com.yudillo.api.global.response.SuccessMessage;
import com.yudillo.api.global.security.AuthUser;
import com.yudillo.api.global.security.CurrentUser;
import com.yudillo.api.user.dto.ChangePasswordRequest;
import com.yudillo.api.user.dto.UpdateNameRequest;
import com.yudillo.api.user.dto.UserResponse;
import com.yudillo.api.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User", description = "내 정보 (마이페이지)")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "내 정보 조회")
    @GetMapping("/me")
    public ApiResponse<UserResponse> getMe(@CurrentUser AuthUser authUser) {
        return ApiResponse.ok(userService.getMe(authUser.getId()));
    }

    @ApiErrorCodes(ErrorCode.INVALID_INPUT)
    @ApiSuccessMessage(SuccessMessage.NAME_UPDATED)
    @Operation(summary = "사용자 이름 변경")
    @PatchMapping("/me/name")
    public ApiResponse<UserResponse> updateName(@CurrentUser AuthUser authUser,
                                                @Valid @RequestBody UpdateNameRequest request) {
        return ApiResponse.ok(userService.updateName(authUser.getId(), request),
                SuccessMessage.NAME_UPDATED);
    }

    @ApiErrorCodes({ErrorCode.INVALID_INPUT, ErrorCode.PASSWORD_MISMATCH,
                    ErrorCode.SAME_PASSWORD})
    @ApiSuccessMessage(SuccessMessage.PASSWORD_CHANGED)
    @Operation(summary = "비밀번호 변경", description = "변경 후 기존 Refresh 토큰은 모두 폐기된다.")
    @PatchMapping("/me/password")
    public ApiResponse<Void> changePassword(@CurrentUser AuthUser authUser,
                                            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(authUser.getId(), request);
        return ApiResponse.ok(SuccessMessage.PASSWORD_CHANGED);
    }
}
