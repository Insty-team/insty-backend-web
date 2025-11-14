package insty.domain.notification.publisher;

import insty.domain.notification.event.notification.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 알림 이벤트 발행자
 * NotificationEvent를 발행하여 DB에 알림을 저장하도록 트리거
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * 알림 이벤트 발행
     *
     * @param userId      사용자 ID
     * @param type        알림 타입
     * @param title       알림 제목
     * @param message     알림 메시지
     * @param redirectUrl 리다이렉트 URL
     */
    public void publish(Long userId, String type, String title, String message, String redirectUrl) {
        NotificationEvent event = new NotificationEvent(userId, type, title, message, redirectUrl);
        eventPublisher.publishEvent(event);
        log.debug("NotificationEvent 발행 완료 - userId: {}, type: {}", userId, type);
    }
}
