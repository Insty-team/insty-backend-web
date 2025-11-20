package insty.domain.notification.strategy;

import insty.domain.notification.dto.event.NotificationData;
import insty.domain.notification.dto.event.NotificationReq;
import insty.notification.NotificationType;

public interface InAppNotificationStrategy {

    /**
     * 이 전략이 처리하는 알림 타입을 반환
     */
    NotificationType getType();

    /* 인앱 알림 전송 여부 검증 */
    default boolean shouldSendInAppNotification(NotificationReq request) {
        return true;
    }

    /**
     * 알림 데이터를 빌드
     */
    NotificationData buildNotificationData(NotificationReq request);
}
