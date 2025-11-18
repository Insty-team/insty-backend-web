package insty.domain.notification.strategy.impl;

import insty.constants.NotificationConstants;
import insty.domain.notification.dto.NotificationData;
import insty.domain.notification.dto.NotificationRequest;
import insty.domain.notification.strategy.AbstractNotificationStrategy;
import insty.domain.notification.util.NotificationUtils;
import insty.notification.NotificationType;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * 새로운 답변 알림
 */
@Component
public class NewAnswerNotificationStrategy extends AbstractNotificationStrategy {

    public NewAnswerNotificationStrategy(NotificationUtils notificationUtils) {
        super(notificationUtils);
    }

    @Override
    public NotificationType getType() {
        return NotificationType.NEW_COMMUNITY_ANSWER;
    }

    // ==================== 인앱 알림 ====================
    @Override
    public NotificationData buildNotificationData(NotificationRequest request) {
        String questionTitle = request.getQuestionTitle();
        String answerAuthorNickname = request.getAnswerAuthorNickname();
        Long questionId = request.getQuestionId();
        Long answerId = request.getAnswerId();

        String title = "새로운 답변이 달렸습니다";
        String message = String.format("%s님이 '%s'에 답변을 작성했습니다",
                answerAuthorNickname,
                truncate(questionTitle, NotificationConstants.TITLE_MAX_LENGTH));
        String redirectUrl = notificationUtils.buildAnswerUrl(questionId, answerId);

        return new NotificationData(title, message, redirectUrl);
    }

    // ==================== 이메일 ====================
    @Override
    protected Map<String, Object> buildEmailContext(NotificationRequest request) {
        Map<String, Object> context = new HashMap<>(request.context());
        Long questionId = request.getQuestionId();
        Long answerId = request.getAnswerId();

        context.put("questionUrl", notificationUtils.buildQuestionUrl(questionId));
        context.put("answerUrl", notificationUtils.buildAnswerUrl(questionId, answerId));

        return context;
    }
}
