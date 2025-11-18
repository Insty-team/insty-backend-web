package insty.domain.notification.strategy;

import insty.domain.notification.dto.NotificationRequest;
import insty.mail.MailContent;
import insty.notification.NotificationType;

public interface EmailNotificationStrategy {

    /* 이 전략이 처리하는 알림 타입을 반환 */
    NotificationType getType();

    /* 이메일 전송 여부 검증 */
    default boolean shouldSendEmail(NotificationRequest request) {
        return true;
    }

    /* 이메일 컨텐츠를 빌드 */
    MailContent buildMailContent(NotificationRequest request, String recipientEmail);
}
