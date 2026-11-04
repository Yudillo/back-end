package com.yudillo.api.auth.entity;

import java.time.Duration;

/** 메일 토큰의 용도. 용도마다 유효기간이 다르다. */
public enum TokenPurpose {

    /** 비밀번호 재설정. 유효기간 30분 */
    PASSWORD_RESET(Duration.ofMinutes(30)),
    /** 회원가입 이메일 인증. 유효기간 1일 ( 24시간 ) */
    EMAIL_VERIFY(Duration.ofHours(24));

    private final Duration validity;

    TokenPurpose(Duration validity) {
        this.validity = validity;
    }

    public Duration validity() {
        return validity;
    }
}
