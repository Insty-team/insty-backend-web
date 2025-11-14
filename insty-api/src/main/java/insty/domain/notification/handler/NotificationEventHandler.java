package insty.domain.notification.handler;

import insty.domain.notification.event.notification.NotificationEvent;
import insty.domain.notification.repository.NotificationRepository;
import insty.model.notification.Notification;
import insty.model.notification.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 알림 저장 이벤트 핸들러
 * NotificationEvent를 수신하여 DB에 알림을 저장
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventHandler {

    private final NotificationRepository notificationRepository;

    @Async
    @EventListener
    @Transactional
    public void handle(NotificationEvent event) {
        try {
            NotificationType notificationType = NotificationType.valueOf(event.type());

            Notification notification = Notification.create(
                    event.userId(),
                    notificationType,
                    event.title(),
                    event.message(),
                    event.redirectUrl()
            );

            notificationRepository.save(notification);
            log.info("알림 저장 완료 - userId: {}, type: {}, title: {}", event.userId(), event.type(), event.title());
        } catch (IllegalArgumentException e) {
            log.error("잘못된 NotificationType - type: {}", event.type(), e);
        } catch (Exception e) {
            log.error("알림 저장 실패 - userId: {}, type: {}", event.userId(), event.type(), e);
        }
    }
}
