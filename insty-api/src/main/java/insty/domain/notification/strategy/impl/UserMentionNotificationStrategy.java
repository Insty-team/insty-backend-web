package insty.domain.notification.strategy.impl;

import insty.constants.NotificationConstants;
import insty.domain.notification.strategy.NotificationData;
import insty.domain.notification.strategy.NotificationStrategy;
import insty.domain.notification.util.NotificationUrlBuilder;
import insty.model.user.UserNotificationPreference;
import insty.notification.NotificationRequest;
import insty.notification.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;


@Component
@RequiredArgsConstructor
public class UserMentionNotificationStrategy implements NotificationStrategy {

    private final NotificationUrlBuilder urlBuilder;

    @Override
    public NotificationType getType() {
        return NotificationType.USER_MENTIONED;
    }

    @Override
    public boolean shouldNotify(NotificationRequest request, UserNotificationPreference preference) {
        return preference.isUserMentionNotificationEnabled();
    }

    @Override
    public boolean shouldSendEmail(NotificationRequest request, UserNotificationPreference preference) {
        return preference.shouldReceiveUserMentionEmail();
    }

    @Override
    public NotificationData buildNotification(NotificationRequest request) {
        String mentionerNickname = request.getMentionerNickname();
        String content = request.getContent();
        String contentType = request.getContentType();
        Long relatedId = request.getRelatedId();

        String title = "누군가 당신을 언급했습니다";
        String message = String.format("%s님이 당신을 언급했습니다: %s",
                mentionerNickname,
                truncate(content, NotificationConstants.CONTENT_MAX_LENGTH));
        String redirectUrl = urlBuilder.buildMentionUrl(contentType, relatedId);

        return new NotificationData(title, message, redirectUrl);
    }

    @Override
    public Map<String, Object> buildEmailContext(NotificationRequest request) {
        Map<String, Object> context = new HashMap<>(request.context());
        String contentType = request.getContentType();
        Long relatedId = request.getRelatedId();

        context.put("mentionUrl", urlBuilder.buildMentionUrl(contentType, relatedId));

        return context;
    }

    private String truncate(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }
}
