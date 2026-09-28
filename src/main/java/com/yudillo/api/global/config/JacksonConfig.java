package com.yudillo.api.global.config;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.module.SimpleModule;

/**
 * 모든 응답의 Instant 를 app.datetime 설정대로 직렬화한다.
 *
 * <p>DTO 마다 @JsonFormat 을 붙이면 빠뜨리는 필드가 생기고 형식이 제각각이 된다.
 * 여기 한 곳에서 처리하면 응답 전체가 항상 같은 형식으로 나간다.
 *
 * <p>개별 필드에 @JsonFormat 을 붙이면 그 필드만 여기 설정을 무시한다(필드 우선).
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class JacksonConfig {

    private final DateTimeProperties dateTimeProperties;

    @Bean
    public JsonMapperBuilderCustomizer dateTimeFormatCustomizer() {
        if (dateTimeProperties.useIso()) {
            log.info("날짜 직렬화: ISO-8601 (Jackson 기본)");
            return builder -> { };
        }

        DateTimeFormatter formatter = dateTimeProperties.formatter();
        log.info("날짜 직렬화: pattern={}, zone={}",
                dateTimeProperties.pattern(), dateTimeProperties.zone());

        SimpleModule module = new SimpleModule("yudillo-datetime")
                .addSerializer(Instant.class, new InstantSerializer(formatter))
                .addDeserializer(Instant.class, new InstantDeserializer(formatter));

        return builder -> builder.addModule(module);
    }

    /** Instant(UTC) -> 설정된 시간대의 문자열. */
    private static final class InstantSerializer extends ValueSerializer<Instant> {

        private final DateTimeFormatter formatter;

        private InstantSerializer(DateTimeFormatter formatter) {
            this.formatter = formatter;
        }

        @Override
        public void serialize(Instant value, JsonGenerator gen, SerializationContext ctxt) {
            gen.writeString(formatter.format(value));
        }
    }

    /**
     * 문자열 -> Instant. 설정된 패턴을 먼저 시도하고, 실패하면 ISO-8601 로 한 번 더 시도한다.
     * 프론트가 서버에서 받은 값을 그대로 돌려보내는 경우와, ISO 로 보내는 경우를 모두 받아준다.
     */
    private static final class InstantDeserializer extends ValueDeserializer<Instant> {

        private final DateTimeFormatter formatter;

        private InstantDeserializer(DateTimeFormatter formatter) {
            this.formatter = formatter;
        }

        @Override
        public Instant deserialize(JsonParser p, DeserializationContext ctxt) {
            String text = p.getString();
            if (text == null || text.isBlank()) {
                return null;
            }
            try {
                return LocalDateTime.parse(text.trim(), formatter)
                        .atZone(formatter.getZone())
                        .toInstant();
            } catch (DateTimeParseException e) {
                return Instant.parse(text.trim());
            }
        }
    }
}
