package com.yudillo.api.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

@Configuration
public class SuccessResponseCustomizer {

    private static final String JSON = org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
    private static final String EXTENSION = "x-success-message";
    private static final String SCHEMA_PREFIX = "#/components/schemas/";
    private static final int MAX_DEPTH = 5;

    /** 1단계: 애노테이션의 문구를 오퍼레이션에 기록해 둔다. */
    @Bean
    public OperationCustomizer successMessageMarker() {
        return (Operation operation, HandlerMethod handlerMethod) -> {
            ApiSuccessMessage annotation = handlerMethod.getMethodAnnotation(ApiSuccessMessage.class);
            if (annotation != null) {
                operation.addExtension(EXTENSION, annotation.value().getMessage());
            }
            return operation;
        };
    }

    /** 2단계: 기록해 둔 문구로 2xx 응답의 예시를 만든다. */
    @Bean
    public OpenApiCustomizer successExampleCustomizer() {
        return openApi -> {
            if (openApi.getPaths() == null || openApi.getComponents() == null) {
                return;
            }
            openApi.getPaths().values().forEach(pathItem ->
                    pathItem.readOperations().forEach(op -> applyExample(op, openApi.getComponents())));
        };
    }

    private void applyExample(Operation operation, Components components) {
        if (operation.getExtensions() == null || operation.getResponses() == null) {
            return;
        }
        Object message = operation.getExtensions().get(EXTENSION);
        if (message == null) {
            return;
        }
        operation.getExtensions().remove(EXTENSION);   // 문서에 노출할 값은 아니다

        operation.getResponses().forEach((status, response) -> {
            if (!status.startsWith("2")) {
                return;
            }
            MediaType mediaType = jsonMediaType(response);
            if (mediaType == null || mediaType.getSchema() == null) {
                return;
            }
            Object example = resolve(mediaType.getSchema(), components, 0);
            if (example instanceof Map<?, ?> map && map.containsKey("message")) {
                Map<String, Object> body = new LinkedHashMap<>();
                map.forEach((k, v) -> body.put(String.valueOf(k), v));
                body.put("message", message);
                // ApiResponse<Void> 는 data 가 null 이라 @JsonInclude(NON_NULL) 로 응답에서 빠진다.
                // 예시에도 넣지 않아야 실제 응답과 같아진다.
                if (isVoidData(mediaType.getSchema())) {
                    body.remove("data");
                }
                mediaType.setExample(body);
            }
        });
    }

    private boolean isVoidData(Schema<?> schema) {
        String ref = schema.get$ref();
        return ref != null && ref.endsWith("Void");
    }

    private MediaType jsonMediaType(ApiResponse response) {
        return response.getContent() == null ? null : response.getContent().get(JSON);
    }

    /** 스키마를 따라가며 example 값으로 채운 객체를 만든다. $ref 는 components 에서 찾아 펼친다. */
    private Object resolve(Schema<?> schema, Components components, int depth) {
        if (schema == null || depth > MAX_DEPTH) {
            return null;
        }
        if (schema.get$ref() != null) {
            Schema<?> target = lookup(schema.get$ref(), components);
            return target == null ? null : resolve(target, components, depth + 1);
        }
        if (schema.getExample() != null) {
            return schema.getExample();
        }
        if ("array".equals(schema.getType())) {
            Object item = resolve(schema.getItems(), components, depth + 1);
            List<Object> list = new ArrayList<>();
            if (item != null) {
                list.add(item);
            }
            return list;
        }
        if (schema.getProperties() != null) {
            Map<String, Object> object = new LinkedHashMap<>();
            schema.getProperties().forEach((name, property) ->
                    object.put(name, resolve(property, components, depth + 1)));
            return object;
        }
        return placeholder(schema);
    }

    private Schema<?> lookup(String ref, Components components) {
        if (components.getSchemas() == null) {
            return null;
        }
        return components.getSchemas().get(ref.substring(SCHEMA_PREFIX.length()));
    }

    /** example 이 없는 필드는 Swagger 기본 표시와 같은 값으로 채운다. */
    private Object placeholder(Schema<?> schema) {
        if (schema.getEnum() != null && !schema.getEnum().isEmpty()) {
            return schema.getEnum().get(0);
        }
        return switch (String.valueOf(schema.getType())) {
            case "boolean" -> true;
            case "integer" -> 0;
            case "number" -> 0.0;
            case "object" -> Map.of();
            default -> "string";
        };
    }
}
