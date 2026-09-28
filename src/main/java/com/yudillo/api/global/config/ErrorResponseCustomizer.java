package com.yudillo.api.global.config;

import com.yudillo.api.global.exception.ErrorCode;
import com.yudillo.api.global.exception.ErrorResponse;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponses;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

/**
 * 에러 응답을 Swagger 문서에 자동으로 채운다.
 *
 * <p>컨트롤러에 {@link ApiErrorCodes} 로 ErrorCode 만 나열하면
 * HTTP 상태코드 · 설명 · 예시 JSON 을 여기서 생성한다.
 * 공통인 401/500 은 애노테이션 없이도 자동으로 붙는다.
 *
 * <p>덕분에 {@link ErrorCode} 의 메시지를 고치면 문서도 같이 바뀐다.
 */
@Configuration
@RequiredArgsConstructor
public class ErrorResponseCustomizer {

    private static final String JSON = org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
    private static final String ERROR_SCHEMA_REF = "#/components/schemas/ErrorResponse";

    private final DateTimeProperties dateTimeProperties;

    /**
     * ErrorResponse 스키마를 components 에 등록한다.
     * 어떤 엔드포인트도 이 타입을 직접 반환하지 않아 springdoc 이 자동 등록하지 않기 때문이다.
     */
    @Bean
    public OpenApiCustomizer errorSchemaRegistrar() {
        return openApi -> {
            // read() 는 최상위 스키마만 돌려줘 중첩 타입(FieldError)이 누락된다.
            // readAll() 이어야 참조된 하위 스키마까지 함께 등록된다.
            Map<String, Schema> schemas = ModelConverters.getInstance().readAll(ErrorResponse.class);
            if (openApi.getComponents() == null) {
                openApi.setComponents(new Components());
            }
            schemas.forEach(openApi.getComponents()::addSchemas);
        };
    }

    // 메서드 이름을 클래스명(errorResponseCustomizer)과 다르게 둔다.
    // 같으면 설정 클래스 빈과 이름이 겹쳐 기동이 실패한다.
    @Bean
    public OperationCustomizer errorResponseOperationCustomizer() {
        return this::customize;
    }

    private Operation customize(Operation operation, HandlerMethod handlerMethod) {
        List<ErrorCode> codes = new ArrayList<>();

        ApiErrorCodes declared = handlerMethod.getMethodAnnotation(ApiErrorCodes.class);
        if (declared != null) {
            codes.addAll(List.of(declared.value()));
        }
        // 인증이 필요한 경로면 토큰 관련 실패가 항상 가능하다.
        if (requiresAuth(operation)) {
            codes.add(ErrorCode.UNAUTHORIZED);
            codes.add(ErrorCode.INVALID_TOKEN);
            codes.add(ErrorCode.EXPIRED_TOKEN);
        }
        codes.add(ErrorCode.INTERNAL_ERROR);

        if (operation.getResponses() == null) {
            operation.setResponses(new ApiResponses());
        }

        // 같은 상태코드끼리 묶는다. HTTP 응답 하나에 여러 ErrorCode 가 대응할 수 있다.
        groupByStatus(codes).forEach((status, group) ->
                operation.getResponses().addApiResponse(status, buildResponse(group)));

        return operation;
    }

    /**
     * springdoc 은 {@code @SecurityRequirements}(값 없음)가 붙은 엔드포인트에
     * 빈 security 목록을 설정한다. 그 경우가 인증이 필요 없는 경로다.
     */
    private boolean requiresAuth(Operation operation) {
        return operation.getSecurity() == null || !operation.getSecurity().isEmpty();
    }

    private Map<String, List<ErrorCode>> groupByStatus(List<ErrorCode> codes) {
        return codes.stream()
                .distinct()
                .collect(Collectors.groupingBy(
                        code -> String.valueOf(code.getStatus().value()),
                        LinkedHashMap::new,
                        Collectors.toList()));
    }

    private io.swagger.v3.oas.models.responses.ApiResponse buildResponse(List<ErrorCode> codes) {
        Map<String, Example> examples = new LinkedHashMap<>();
        codes.forEach(code -> examples.put(code.getCode(),
                new Example().summary(code.getCode() + " " + code.getMessage())
                        .value(exampleJson(code))));

        MediaType mediaType = new MediaType()
                .schema(new Schema<>().$ref(ERROR_SCHEMA_REF))
                .examples(examples);

        String description = codes.stream()
                .map(code -> code.getCode() + " " + code.getMessage())
                .collect(Collectors.joining(" / "));

        return new io.swagger.v3.oas.models.responses.ApiResponse()
                .description(description)
                .content(new Content().addMediaType(JSON, mediaType));
    }

    /** 실제 응답과 같은 모양의 예시를 만든다. 날짜 형식도 app.datetime 설정을 따른다. */
    private Map<String, Object> exampleJson(ErrorCode code) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("code", code.getCode());
        body.put("message", code.getMessage());
        if (code == ErrorCode.INVALID_INPUT) {
            body.put("errors", List.of(
                    Map.of("field", "email", "reason", "이메일 형식으로 입력하세요.")));
        }
        body.put("timestamp", formatNow());
        return body;
    }

    private String formatNow() {
        Instant now = Instant.now();
        return dateTimeProperties.useIso()
                ? now.toString()
                : dateTimeProperties.formatter().format(now);
    }
}
