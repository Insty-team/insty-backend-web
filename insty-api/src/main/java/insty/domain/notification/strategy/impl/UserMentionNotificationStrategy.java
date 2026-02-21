package insty.domain.notification.strategy.impl;

import insty.constants.NotificationConstants;
import insty.domain.notification.dto.event.NotificationData;
import insty.domain.notification.dto.event.NotificationReq;
import insty.domain.notification.strategy.AbstractNotificationStrategy;
import insty.domain.notification.util.NotificationUtils;
import insty.notification.NotificationType;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

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

    @Override
    public NotificationData buildNotificationData(NotificationReq request) {
        String mentionerNickname = request.getMentionerNickname();
        String content = request.getContent();

        String title = "누군가 당신을 언급했습니다";
        String message = String.format("%s님이 당신을 언급했습니다: %s",
                mentionerNickname,
                truncate(content, NotificationConstants.CONTENT_MAX_LENGTH));
        String redirectUrl = resolveMentionUrl(request);

        return new NotificationData(title, message, redirectUrl);
    }

    // ==================== 이메일 ====================

    @Override
    protected Map<String, Object> buildEmailContext(NotificationReq request) {
        Map<String, Object> context = new HashMap<>(request.context());

        context.put("mentionUrl", resolveMentionUrl(request));

        return context;
    }

    private String resolveMentionUrl(NotificationReq request) {
        String contentType = request.getContentType();
        Long relatedId = request.getRelatedId();

        if (isAnswerContentType(contentType) && request.getAnswerId() != null) {
            return notificationUtils.buildMentionUrl(contentType, relatedId, request.getAnswerId());
        }
        return notificationUtils.buildMentionUrl(contentType, relatedId);
    }

    private boolean isAnswerContentType(String contentType) {
        return "ANSWER".equals(contentType) || "COURSE_ANSWER".equals(contentType);
    }
}
