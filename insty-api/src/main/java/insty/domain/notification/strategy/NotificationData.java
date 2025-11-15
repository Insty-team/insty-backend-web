package insty.domain.notification.strategy;

/**
 * 알림 데이터 DTO
 * 알림 엔티티 생성에 필요한 데이터를 담는 불변 객체
 */
public record NotificationData(
        String title,
        String message,
        String redirectUrl
) {
}
