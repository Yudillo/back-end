package com.yudillo.api.global.response;

import lombok.Getter;

/**
 * 성공 응답의 안내 문구. {@code ErrorCode} 와 짝을 이루는 성공 쪽 단일 출처다.
 *
 * <p>컨트롤러에 문자열을 직접 쓰면 Swagger 문서와 실제 응답이 어긋난다.
 * enum 으로 두면 응답과 문서가 같은 값을 보게 된다.
 *
 * <pre>
 * &#64;ApiSuccessMessage(SuccessMessage.SIGNUP)   // 문서
 * ...
 * return ApiResponse.ok(data, SuccessMessage.SIGNUP);   // 실제 응답
 * </pre>
 */
@Getter
public enum SuccessMessage {

    SIGNUP("회원가입이 완료되었습니다."),
    LOGIN("로그인에 성공했습니다."),
    LOGOUT("로그아웃되었습니다."),
    NAME_UPDATED("이름이 변경되었습니다."),
    PASSWORD_CHANGED("비밀번호가 변경되었습니다. 다시 로그인해 주세요.");

    private final String message;

    SuccessMessage(String message) {
        this.message = message;
    }
}
