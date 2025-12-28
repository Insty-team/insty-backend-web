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

/**
 * 새로운 강좌 질문 알림
 */
@Component
public class NewQuestionNotificationStrategy extends AbstractNotificationStrategy {

    public NewQuestionNotificationStrategy(NotificationUtils notificationUtils) {
        super(notificationUtils);
    }

    @Override
    public NotificationType getType() {
        return NotificationType.NEW_COURSE_QUESTION;
    }

    // ==================== 인앱 알림 ====================

    @Override
    public NotificationData buildNotificationData(NotificationReq request) {
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

    @Override
    protected Map<String, Object> buildEmailContext(NotificationReq request) {
        Map<String, Object> context = new HashMap<>(request.context());
        context.put("questionUrl", notificationUtils.buildQuestionUrl(request.getQuestionId()));
        return context;
    }
}
