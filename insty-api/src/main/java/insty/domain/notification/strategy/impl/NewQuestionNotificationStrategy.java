package insty.domain.notification.strategy.impl;

import insty.constants.NotificationConstants;
import insty.domain.notification.common.NotificationUtils;
import insty.domain.notification.strategy.AbstractNotificationStrategy;
import insty.domain.notification.strategy.NotificationData;
import insty.model.user.UserNotificationPreference;
import insty.notification.NotificationRequest;
import insty.notification.NotificationType;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 새로운 커뮤니티 질문 알림 전략
 * 인앱 알림 + 이메일 모두 지원
 */
@Component
public class NewQuestionNotificationStrategy extends AbstractNotificationStrategy {

    public NewQuestionNotificationStrategy(NotificationUtils notificationUtils) {
        super(notificationUtils);
    }

    @Override
    public NotificationType getType() {
        return NotificationType.NEW_COMMUNITY_QUESTION;
    }

    // ==================== 인앱 알림 ====================

    @Override
    public boolean shouldSendInAppNotification(NotificationRequest request, UserNotificationPreference preference) {
        return preference.isNewQuestionNotificationEnabled();
    }

    @Override
    public NotificationData buildNotificationData(NotificationRequest request) {
        String questionTitle = request.getQuestionTitle();
        String questionAuthorName = request.getQuestionAuthorName();
        String courseName = request.getCourseName();
        Long questionId = request.getQuestionId();

        String title = "새로운 질문이 등록되었습니다";
        String message = String.format("%s님이 '%s' 강의에 '%s' 질문을 등록했습니다",
                questionAuthorName,
                courseName,
                truncate(questionTitle, NotificationConstants.TITLE_MAX_LENGTH));
        String redirectUrl = notificationUtils.buildQuestionUrl(questionId);

        return new NotificationData(title, message, redirectUrl);
    }

    // ==================== 이메일 ====================

    @Override
    public boolean shouldSendEmail(NotificationRequest request, UserNotificationPreference preference) {
        return preference.shouldReceiveNewQuestionEmail();
    }

    @Override
    public Map<String, Object> buildEmailContext(NotificationRequest request) {
        Map<String, Object> context = new HashMap<>(request.context());
        context.put("questionUrl", notificationUtils.buildQuestionUrl(request.getQuestionId()));
        return context;
    }
}
