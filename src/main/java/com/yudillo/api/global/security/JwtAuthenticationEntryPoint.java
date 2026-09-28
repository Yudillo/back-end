package com.yudillo.api.global.security;

import com.yudillo.api.global.exception.ErrorCode;
import com.yudillo.api.global.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/** 인증 실패(401)를 GlobalExceptionHandler 와 동일한 JSON 포맷으로 내려준다. */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final JsonMapper jsonMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        ErrorCode errorCode = resolve(authException);
        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        jsonMapper.writeValue(response.getWriter(),
                ErrorResponse.of(errorCode, errorCode.getMessage()));
    }

    private ErrorCode resolve(AuthenticationException e) {
        if (e instanceof JwtAuthenticationException) {
            String message = e.getMessage();
            return message != null && message.contains("만료")
                    ? ErrorCode.EXPIRED_TOKEN
                    : ErrorCode.INVALID_TOKEN;
        }
        return ErrorCode.UNAUTHORIZED;
    }
}
