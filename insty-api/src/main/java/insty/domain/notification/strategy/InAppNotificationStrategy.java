package insty.domain.notification.strategy;

import insty.domain.notification.dto.NotificationData;
import insty.domain.notification.dto.NotificationRequest;
import insty.notification.NotificationType;

public interface InAppNotificationStrategy {

    /**
     * 이 전략이 처리하는 알림 타입을 반환
     */
    NotificationType getType();

    /* 인앱 알림 전송 여부 검증 */
    default boolean shouldSendInAppNotification(NotificationRequest request) {
        return true;
    }

    /**
     * 알림 데이터를 빌드
     */
    NotificationData buildNotificationData(NotificationRequest request);
}
