package insty.trackevent.port;

import insty.trackevent.model.MixpanelEventType;
import java.util.Map;

// 외부로 보내는 전용 포트를 통해, 분석 이벤트 발행을 추상화하는 목적
public interface AnalyticsEventPublisher {
    void publish(MixpanelEventType eventType, Long distinctId, Map<String, Object> properties);
}