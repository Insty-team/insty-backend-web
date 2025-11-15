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

/**
 * 새로운 커뮤니티 질문 알림 전략
 */
@Component
@RequiredArgsConstructor
public class NewQuestionNotificationStrategy implements NotificationStrategy {

    private final NotificationUrlBuilder urlBuilder;

    @Override
    public NotificationType getType() {
        return NotificationType.NEW_COMMUNITY_QUESTION;
    }

    @Override
    public boolean shouldNotify(NotificationRequest request, UserNotificationPreference preference) {
        return preference.isNewQuestionNotificationEnabled();
    }

    @Override
    public boolean shouldSendEmail(NotificationRequest request, UserNotificationPreference preference) {
        return preference.shouldReceiveNewQuestionEmail();
    }

    @Override
    public NotificationData buildNotification(NotificationRequest request) {
        String questionTitle = request.getQuestionTitle();
        String questionAuthorName = request.getQuestionAuthorName();
        String courseName = request.getCourseName();
        Long questionId = request.getQuestionId();

        String title = "새로운 질문이 등록되었습니다";
        String message = String.format("%s님이 '%s' 강의에 '%s' 질문을 등록했습니다",
                questionAuthorName,
                courseName,
                truncate(questionTitle, NotificationConstants.TITLE_MAX_LENGTH));
        String redirectUrl = urlBuilder.buildQuestionUrl(questionId);

        return new NotificationData(title, message, redirectUrl);
    }

    @Override
    public Map<String, Object> buildEmailContext(NotificationRequest request) {
        Map<String, Object> context = new HashMap<>(request.context());
        context.put("questionUrl", urlBuilder.buildQuestionUrl(request.getQuestionId()));
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
