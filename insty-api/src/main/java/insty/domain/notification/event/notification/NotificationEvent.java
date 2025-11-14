package insty.domain.notification.event.notification;

/**
 * 알림 저장 이벤트
 * DB에 알림을 저장하기 위한 이벤트
 */
public record NotificationEvent(
        Long userId,
        String type,
        String title,
        String message,
        String redirectUrl
) {
}
