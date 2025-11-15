package insty.domain.notification.strategy;

import insty.model.user.UserNotificationPreference;
import insty.notification.NotificationRequest;
import insty.notification.NotificationType;

/**
 * 인앱 알림 전략 인터페이스
 * 알림 저장 및 표시에 대한 책임만 가짐
 */
public interface InAppNotificationStrategy {

    /**
     * 이 전략이 처리하는 알림 타입을 반환
     */
    NotificationType getType();

    /**
     * 인앱 알림을 전송해야 하는지 검증
     *
     * @param request 알림 요청 데이터
     * @param preference 사용자 알림 설정
     * @return 알림 전송 여부
     */
    boolean shouldSendInAppNotification(NotificationRequest request, UserNotificationPreference preference);

    /**
     * 알림 데이터를 빌드
     *
     * @param request 알림 요청 데이터
     * @return 알림 데이터 (title, message, redirectUrl)
     */
    NotificationData buildNotificationData(NotificationRequest request);
}
