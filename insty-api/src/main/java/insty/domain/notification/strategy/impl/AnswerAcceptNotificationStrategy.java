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
 * 답변 채택 알림 전략
 */
@Component
@RequiredArgsConstructor
public class AnswerAcceptNotificationStrategy implements NotificationStrategy {

    private final NotificationUrlBuilder urlBuilder;

    @Override
    public NotificationType getType() {
        return NotificationType.COMMUNITY_ANSWER_ACCEPT;
    }

    @Override
    public boolean shouldNotify(NotificationRequest request, UserNotificationPreference preference) {
        return preference.isAnswerAcceptedNotificationEnabled();
    }

    @Override
    public boolean shouldSendEmail(NotificationRequest request, UserNotificationPreference preference) {
        return preference.shouldReceiveAnswerAcceptedEmail();
    }

    @Override
    public NotificationData buildNotification(NotificationRequest request) {
        String questionTitle = request.getQuestionTitle();
        Long questionId = request.getQuestionId();
        Long answerId = request.getAnswerId();

        String title = "답변이 채택되었습니다";
        String message = String.format("'%s'에 작성한 답변이 채택되었습니다",
                truncate(questionTitle, NotificationConstants.TITLE_MAX_LENGTH));
        String redirectUrl = urlBuilder.buildAnswerUrl(questionId, answerId);

        return new NotificationData(title, message, redirectUrl);
    }

    @Override
    public Map<String, Object> buildEmailContext(NotificationRequest request) {
        Map<String, Object> context = new HashMap<>(request.context());
        Long questionId = request.getQuestionId();
        Long answerId = request.getAnswerId();

        context.put("questionUrl", urlBuilder.buildQuestionUrl(questionId));
        context.put("answerUrl", urlBuilder.buildAnswerUrl(questionId, answerId));

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
