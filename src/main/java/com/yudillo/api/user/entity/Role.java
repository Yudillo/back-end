package com.yudillo.api.user.entity;

/** 서비스 전역 권한. 프로젝트 내부 권한은 ProjectRole 이 따로 관리한다. */
public enum Role {
    USER,
    ADMIN;

    public String authority() {
        return "ROLE_" + name();
    }
}
