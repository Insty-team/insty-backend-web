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
 * 답변 채택 알림 전략
 * 인앱 알림 + 이메일 모두 지원
 */
@Component
public class AnswerAcceptNotificationStrategy extends AbstractNotificationStrategy {

    public AnswerAcceptNotificationStrategy(NotificationUtils notificationUtils) {
        super(notificationUtils);
    }

    @Override
    public NotificationType getType() {
        return NotificationType.COMMUNITY_ANSWER_ACCEPT;
    }

    // ==================== 인앱 알림 ====================

    @Override
    public boolean shouldSendInAppNotification(NotificationRequest request, UserNotificationPreference preference) {
        return preference.isAnswerAcceptedNotificationEnabled();
    }

    @Override
    public NotificationData buildNotificationData(NotificationRequest request) {
        String questionTitle = request.getQuestionTitle();
        Long questionId = request.getQuestionId();
        Long answerId = request.getAnswerId();

        String title = "답변이 채택되었습니다";
        String message = String.format("'%s'에 작성한 답변이 채택되었습니다",
                truncate(questionTitle, NotificationConstants.TITLE_MAX_LENGTH));
        String redirectUrl = notificationUtils.buildAnswerUrl(questionId, answerId);

        return new NotificationData(title, message, redirectUrl);
    }

    // ==================== 이메일 ====================

    @Override
    public boolean shouldSendEmail(NotificationRequest request, UserNotificationPreference preference) {
        return preference.shouldReceiveAnswerAcceptedEmail();
    }

    @Override
    public Map<String, Object> buildEmailContext(NotificationRequest request) {
        Map<String, Object> context = new HashMap<>(request.context());
        Long questionId = request.getQuestionId();
        Long answerId = request.getAnswerId();

        context.put("questionUrl", notificationUtils.buildQuestionUrl(questionId));
        context.put("answerUrl", notificationUtils.buildAnswerUrl(questionId, answerId));

        return context;
    }
}
