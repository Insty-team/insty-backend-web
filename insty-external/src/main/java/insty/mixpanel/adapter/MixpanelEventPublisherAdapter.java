package insty.mixpanel.adapter;

import insty.trackevent.model.MixpanelEventType;
import insty.trackevent.port.AnalyticsEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class MixpanelEventPublisherAdapter implements AnalyticsEventPublisher {

    // Mixpanel 이벤트 수집 엔드포인트
    private static final String MIXPANEL_TRACK_ENDPOINT = "https://api.mixpanel.com/track";

    private final WebClient webClient;

    // Mixpanel 프로젝트 토큰
    private final String projectToken;

    @Override
    public void publish(MixpanelEventType eventType, Long distinctId, Map<String, Object> properties) {

        Map<String, Object> body = buildMixpanelTrackRequestBody(eventType, distinctId, properties);

        // 비동기 전송
        // Fire and Forget 처리
        webClient.post()
                .uri(MIXPANEL_TRACK_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .toBodilessEntity()
                .doOnError(exception -> log.warn("Failed to publish Mixpanel event: {}, error={}", eventType, exception.toString()))
                .onErrorComplete()
                .subscribe();
    }

    private Map<String, Object> buildMixpanelTrackRequestBody(MixpanelEventType eventType, Long distinctId,
                                                              Map<String, Object> properties) {
        if (projectToken == null || projectToken.isBlank()) {
            throw new IllegalArgumentException("Mixpanel token is missing");
        }

        // 기존 속성에 token, distinct_id를 추가해 Mixpanel properties 구성
        Map<String, Object> props = new HashMap<>(properties);
        props.put("token", projectToken);
        props.put("distinct_id", distinctId);

        // 최종 body 구성
        Map<String, Object> body = new HashMap<>();
        body.put("event", eventType.name());
        body.put("properties", props);
        return body;
    }
}