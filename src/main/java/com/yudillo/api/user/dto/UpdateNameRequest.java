package com.yudillo.api.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(name = "UpdateNameRequest", description = "사용자 이름 변경 요청")
public record UpdateNameRequest(

        @Schema(description = "새 이름 (2~10자)", example = "홍길동")
        @NotBlank(message = "이름을 입력하세요.")
        @Pattern(regexp = "^[가-힣a-zA-Z0-9]{2,10}$", message = "형식에 맞는 이름을 입력하세요.")
        String name
) {
}
