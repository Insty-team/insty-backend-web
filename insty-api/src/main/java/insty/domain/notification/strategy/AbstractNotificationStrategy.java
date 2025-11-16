package insty.domain.notification.strategy;

import insty.domain.notification.util.NotificationUtils;
import insty.mail.MailContent;
import insty.mail.MailType;
import insty.domain.notification.mail.NotificationMailContent;
import insty.domain.notification.dto.NotificationRequest;
import insty.notification.NotificationType;
import lombok.RequiredArgsConstructor;

import java.util.Map;

/**
 * 알림 및 이메일 전략의 추상 베이스 클래스
 * 두 인터페이스를 모두 구현하며 공통 로직 제공
 *
 * 서브클래스는 필요한 메서드만 오버라이드하면 됨
 */
@RequiredArgsConstructor
public abstract class AbstractNotificationStrategy
        implements InAppNotificationStrategy, EmailNotificationStrategy {

    protected final NotificationUtils notificationUtils;

    /**
     * 알림 타입 반환 (서브클래스에서 구현 필수)
     */
    @Override
    public abstract NotificationType getType();

    /**
     * 텍스트 자르기 헬퍼 메서드
     */
    protected String truncate(String text, int maxLength) {
        return notificationUtils.truncateContent(text, maxLength);
    }

    /**
     * NotificationType을 MailType으로 매핑
     * 서브클래스에서 오버라이드 가능
     */
    protected MailType getMailType() {
        return switch (getType()) {
            case NEW_COMMUNITY_QUESTION -> MailType.COMMUNITY_QUESTION;
            case NEW_COMMUNITY_ANSWER -> MailType.COMMUNITY_ANSWER;
            case COMMUNITY_ANSWER_ACCEPT -> MailType.COMMUNITY_ANSWER_ACCEPT;
            case USER_MENTIONED -> MailType.MENTION;
            default -> throw new IllegalStateException("매핑되지 않은 NotificationType: " + getType());
        };
    }

    /**
     * 이메일 컨텍스트 빌드 (서브클래스에서 구현)
     * buildMailContent에서 사용됨
     */
    protected abstract Map<String, Object> buildEmailContext(NotificationRequest request);

    /**
     * MailContent 빌드 (기본 구현)
     * 이메일 컨텍스트를 빌드하고 MailContent로 변환
     */
    @Override
    public MailContent buildMailContent(NotificationRequest request, String recipientEmail) {
        Map<String, Object> context = buildEmailContext(request);
        return NotificationMailContent.of(recipientEmail, getMailType(), context);
    }
}
