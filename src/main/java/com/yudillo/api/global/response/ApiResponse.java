package com.yudillo.api.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 성공 응답 공통 포맷. 프론트에서 { success, data, message } 로 일관되게 받는다.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
// name 을 고정하면 제네릭별 스키마가 하나로 뭉개져 data 타입이 문서에 안 나온다.
// 비워두면 springdoc 이 ApiResponseUserResponse 처럼 조합해서 만들어 준다.
@Schema(description = "공통 성공 응답")
public record ApiResponse<T>(
        @Schema(description = "성공 여부", example = "true") boolean success,
        @Schema(description = "응답 본문") T data,
        // example 을 여기 박으면 모든 엔드포인트에 같은 문구가 뜬다.
        // 엔드포인트별 실제 문구는 SuccessResponseCustomizer 가 채운다.
        @Schema(description = "안내 메시지") String message
) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        return new ApiResponse<>(true, data, message);
    }

    public static ApiResponse<Void> ok(String message) {
        return new ApiResponse<>(true, null, message);
    }

    public static <T> ApiResponse<T> ok(T data, SuccessMessage message) {
        return new ApiResponse<>(true, data, message.getMessage());
    }

    public static ApiResponse<Void> ok(SuccessMessage message) {
        return new ApiResponse<>(true, null, message.getMessage());
    }
}
