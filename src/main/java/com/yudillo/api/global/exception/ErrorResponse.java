package com.yudillo.api.global.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ErrorResponse", description = "공통 에러 응답")
public record ErrorResponse(
        @Schema(example = "false") boolean success,
        @Schema(description = "에러 코드", example = "U002") String code,
        @Schema(description = "에러 메시지", example = "이미 사용 중인 이메일입니다.") String message,
        @Schema(description = "필드 단위 검증 실패 목록") List<FieldError> errors,
        // 형식은 app.datetime 설정(JacksonConfig)이 전역으로 결정한다.
        @Schema(description = "발생 시각", example = "2026-09-28 10:38:03")
        Instant timestamp
) {

    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return new ErrorResponse(false, errorCode.getCode(), message, null, Instant.now());
    }

    public static ErrorResponse of(ErrorCode errorCode, String message, List<FieldError> errors) {
        return new ErrorResponse(false, errorCode.getCode(), message, errors, Instant.now());
    }

    @Schema(name = "FieldError")
    public record FieldError(
            @Schema(example = "email") String field,
            @Schema(example = "이메일 형식으로 입력하세요.") String reason
    ) {
    }
}
