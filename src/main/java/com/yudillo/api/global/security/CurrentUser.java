package com.yudillo.api.global.security;

import io.swagger.v3.oas.annotations.Parameter;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

/**
 * 컨트롤러 파라미터에 현재 로그인 사용자를 주입한다.
 * hidden = true 로 Swagger 문서에는 파라미터로 노출하지 않는다.
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@AuthenticationPrincipal
@Parameter(hidden = true)
public @interface CurrentUser {
}
