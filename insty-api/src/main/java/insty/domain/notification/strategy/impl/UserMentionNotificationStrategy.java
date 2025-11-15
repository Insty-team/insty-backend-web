package insty.domain.notification.strategy.impl;

import insty.constants.NotificationConstants;
import insty.domain.notification.common.NotificationUtils;
import insty.domain.notification.strategy.AbstractNotificationStrategy;
import insty.domain.notification.strategy.NotificationData;
import insty.domain.notification.common.NotificationRequest;
import insty.notification.NotificationType;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 사용자 멘션 알림 전략
 * 인앱 알림 + 이메일 모두 지원
 */
@Component
public class UserMentionNotificationStrategy extends AbstractNotificationStrategy {

    public UserMentionNotificationStrategy(NotificationUtils notificationUtils) {
        super(notificationUtils);
    }

    @Override
    public NotificationType getType() {
        return NotificationType.USER_MENTIONED;
    }

    // ==================== 인앱 알림 ====================
    // shouldSendInAppNotification은 기본 구현 사용 (항상 true)

    @Override
    public NotificationData buildNotificationData(NotificationRequest request) {
        String mentionerNickname = request.getMentionerNickname();
        String content = request.getContent();
        String contentType = request.getContentType();
        Long relatedId = request.getRelatedId();

        String title = "누군가 당신을 언급했습니다";
        String message = String.format("%s님이 당신을 언급했습니다: %s",
                mentionerNickname,
                truncate(content, NotificationConstants.CONTENT_MAX_LENGTH));
        String redirectUrl = notificationUtils.buildMentionUrl(contentType, relatedId);

        return new NotificationData(title, message, redirectUrl);
    }

    // ==================== 이메일 ====================
    // shouldSendEmail은 기본 구현 사용 (항상 true)

    @Override
    protected Map<String, Object> buildEmailContext(NotificationRequest request) {
        Map<String, Object> context = new HashMap<>(request.context());
        String contentType = request.getContentType();
        Long relatedId = request.getRelatedId();

        context.put("mentionUrl", notificationUtils.buildMentionUrl(contentType, relatedId));

        return context;
    }
}
