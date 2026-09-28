package com.yudillo.api.global.config;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * API 응답에 실리는 날짜/시각 표기 규칙.
 *
 * <p>저장은 언제나 UTC({@code Instant})로 하고, 밖으로 나갈 때만 여기 설정대로 변환한다.
 * 도메인 코드는 시간대를 신경 쓰지 않는다.
 *
 * <pre>
 * app:
 *   datetime:
 *     pattern: "yyyy-MM-dd HH:mm:ss"
 *     zone: Asia/Seoul
 * </pre>
 *
 * ISO-8601 로 되돌리려면 {@code pattern: iso} 로 두면 된다.
 */
@ConfigurationProperties(prefix = "app.datetime")
public record DateTimeProperties(String pattern, String zone) {

    /** pattern 을 이 값으로 두면 변환하지 않고 Jackson 기본(ISO-8601)을 쓴다. */
    public static final String ISO = "iso";

    private static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String DEFAULT_ZONE = "Asia/Seoul";

    public DateTimeProperties {
        pattern = (pattern == null || pattern.isBlank()) ? DEFAULT_PATTERN : pattern.trim();
        zone = (zone == null || zone.isBlank()) ? DEFAULT_ZONE : zone.trim();
    }

    public boolean useIso() {
        return ISO.equalsIgnoreCase(pattern);
    }

    public ZoneId zoneId() {
        return ZoneId.of(zone);
    }

    /** 잘못된 패턴이면 기동 시점에 실패한다. 런타임에 응답이 깨지는 것보다 낫다. */
    public DateTimeFormatter formatter() {
        return DateTimeFormatter.ofPattern(pattern).withZone(zoneId());
    }
}
