package insty.domain.notification.strategy;

import insty.domain.notification.dto.event.NotificationReq;
import insty.domain.notification.mail.NotificationMailContent;
import insty.domain.notification.util.NotificationUtils;
import insty.mail.MailContent;
import insty.mail.MailType;
import insty.notification.NotificationType;
import java.util.Map;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractNotificationStrategy
        implements InAppNotificationStrategy, EmailNotificationStrategy {

    protected final NotificationUtils notificationUtils;

    /* 알림 타입 반환 (서브클래스에서 구현 필수) */
    @Override
    public abstract NotificationType getType();

    /* 텍스트 자르기 헬퍼 메서드 */
    protected String truncate(String text, int maxLength) {
        return notificationUtils.truncateContent(text, maxLength);
    }

    /* NotificationType을 MailType으로 매핑 */
    protected MailType getMailType() {
        return switch (getType()) {
            case NEW_COMMUNITY_QUESTION -> MailType.COMMUNITY_QUESTION;
            case NEW_COMMUNITY_ANSWER -> MailType.COMMUNITY_ANSWER;
            case COMMUNITY_ANSWER_ACCEPT -> MailType.COMMUNITY_ANSWER_ACCEPT;
            case USER_MENTIONED -> MailType.MENTION;
            default -> throw new IllegalStateException("매핑되지 않은 NotificationType: " + getType());
        };
    }

    /* 이메일 컨텍스트 빌드 (서브클래스에서 구현) */
    protected abstract Map<String, Object> buildEmailContext(NotificationReq request);

    /* MailContent 빌드 (기본 구현) */
    @Override
    public MailContent buildMailContent(NotificationReq request, String recipientEmail) {
        Map<String, Object> context = buildEmailContext(request);
        return NotificationMailContent.of(recipientEmail, getMailType(), context);
    }
}
