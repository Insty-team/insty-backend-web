package insty.mixpanel.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import insty.trackevent.model.MixpanelEventType;
import insty.trackevent.port.AnalyticsEventPublisher;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Slf4j
@RequiredArgsConstructor
public class MixpanelEventPublisherAdapter implements AnalyticsEventPublisher {

    private static final String TRACK_PATH = "/track";
    private static final String PROPERTY_TOKEN = "token";
    private static final String PROPERTY_DISTINCT_ID = "distinct_id";
    private static final String PROPERTY_TIME = "time";
    private static final String PROPERTY_INSERT_ID = "$insert_id";

    private static final String QUERY_VERBOSE_ON = "1";
    private static final String QUERY_STRICT_ON = "1";

    private final WebClient webClient;
    private final String projectToken;
    private final String mixpanelHost;        // ex) api.mixpanel.com | api-eu.mixpanel.com
    private final Boolean verboseResponseEnabled;
    private final Boolean strictValidationEnabled;
    private final Boolean trackingEnabled;
    private final Integer requestTimeoutMillis;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void publish(final MixpanelEventType eventType,
                        final Long distinctId,
                        final Map<String, Object> rawEventProperties) {

        // 설정값 검증
        // 전역 스위치(trackingEnabled), 프로젝트 토큰(projectToken), 호스트(mixpanelHost) 등 유효성 확인
        verifyConfiguration();

        // 이벤트 속성 표준화
        // 요청으로 넘어온 rawEventProperties를 기반으로 null 값 제거
        // token, distinct_id, time(epoch sec), $insert_id(중복 방지용) 보강
        final Map<String, Object> normalizedProperties =
                buildProperties(distinctId, rawEventProperties);

        // 이벤트 JSON 직렬화
        // {"event":"...", "properties":{...}} 형태로 직렬화
        final String eventJson = serializeEventToJson(eventType, normalizedProperties);

        // 전송 바디 생성 (Mixpanel 표준 포맷)
        // JSON → base64 인코딩 → application/x-www-form-urlencoded 의 data=<base64(JSON)>
        final MultiValueMap<String, String> formData =
                buildFormDataFromBase64Json(eventJson);

        // 사전 로깅
        // 요청 시작 로깅 (token은 마스킹)
        log.info("[Mixpanel] publish start event={} distinct_id={} properties(maskedToken)={}",
                eventType, normalizedProperties.get(PROPERTY_DISTINCT_ID),
                buildMaskedTokenProperties(normalizedProperties));

        // 비동기 전송 (fire-and-forget)
        // sendAsyncRequest(formData): WebClient POST /track 호출
        // verbose/strict 쿼리 파라미터 반영, timeout + retry(backoff) 포함
        // 성공 시: 응답 바디(예: {"status":1,...}) INFO 로깅
        // 실패 시: WARN 로깅 후 onErrorResume으로 스트림 종료(업무 흐름 영향 X)
        // subscribe() 호출로 실제 요청 트리거
        sendAsyncRequest(formData)
                .doOnNext(responseBody ->
                        log.info("[Mixpanel] response={}", responseBody)) // 기대: {"status":1,...}
                .doOnError(error ->
                        log.warn("[Mixpanel] publish fail event={} distinct_id={} error={}",
                                eventType, normalizedProperties.get(PROPERTY_DISTINCT_ID), error.toString()))
                .onErrorResume(error -> Mono.empty()) // 실패해도 업무 흐름 영향 X
                .subscribe();
    }

    // 설정값 검증
    private void verifyConfiguration() {
        Assert.isTrue(Boolean.TRUE.equals(trackingEnabled), "Mixpanel disabled");
        Assert.hasText(projectToken, "Mixpanel token must not be blank");
        Assert.hasText(mixpanelHost, "Mixpanel host must not be blank");
        Assert.isTrue(requestTimeoutMillis != null && requestTimeoutMillis > 0, "Timeout must be positive");
    }

    // distinctId/토큰/시간/$insert_id 포함하여 properties 생성
    private Map<String, Object> buildProperties(final Long distinctId,
                                                final Map<String, Object> rawEventProperties) {
        final Map<String, Object> eventProperties = new LinkedHashMap<>();

        if (rawEventProperties != null) {
            rawEventProperties.forEach((key, value) -> {
                if (value != null) eventProperties.put(key, value);
            });
        }

        eventProperties.put(PROPERTY_TOKEN, projectToken);
        eventProperties.put(PROPERTY_DISTINCT_ID, (distinctId == null) ? "anonymous" : String.valueOf(distinctId));
        eventProperties.putIfAbsent(PROPERTY_TIME, Instant.now().getEpochSecond());
        eventProperties.putIfAbsent(PROPERTY_INSERT_ID, UUID.randomUUID().toString());

        return eventProperties;
    }

    // {"event": "...", "properties": {...}} 직렬화
    private String serializeEventToJson(final MixpanelEventType eventType,
                                        final Map<String, Object> normalizedProperties) {
        try {
            final Map<String, Object> eventEnvelope = Map.of(
                    "event", eventType.name(),
                    "properties", normalizedProperties
            );
            return objectMapper.writeValueAsString(eventEnvelope);
        } catch (Exception exception) {
            throw new IllegalArgumentException("Failed to serialize Mixpanel event JSON", exception);
        }
    }

    // data=<base64(JSON)> 형태의 form-urlencoded 바디 생성
    private MultiValueMap<String, String> buildFormDataFromBase64Json(final String eventJson) {
        final String base64EncodedData =
                Base64.getEncoder().encodeToString(eventJson.getBytes(StandardCharsets.UTF_8));

        final MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("data", base64EncodedData);
        return formData;
    }

    // 실제 비동기 전송 (timeout + retry(backoff))
    private Mono<String> sendAsyncRequest(final MultiValueMap<String, String> formData) {
        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host(mixpanelHost)
                        .path(TRACK_PATH)
                        .queryParam("verbose", Boolean.TRUE.equals(verboseResponseEnabled) ? QUERY_VERBOSE_ON : null)
                        .queryParam("strict",  Boolean.TRUE.equals(strictValidationEnabled) ? QUERY_STRICT_ON : null)
                        .build())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofMillis(requestTimeoutMillis))
                .retryWhen(
                        Retry.backoff(2, Duration.ofMillis(300))
                                .maxBackoff(Duration.ofSeconds(2))
                                .jitter(0.5)
                );
    }

    // 토큰 마스킹하여 로깅
    private Map<String, Object> buildMaskedTokenProperties(final Map<String, Object> originalProperties) {
        final Map<String, Object> masked = new LinkedHashMap<>(originalProperties);
        final Object token = masked.get(PROPERTY_TOKEN);
        if (token instanceof String tokenString && tokenString.length() > 6) {
            masked.put(PROPERTY_TOKEN, tokenString.substring(0, 6) + "****");
        }
        return masked;
    }
}