package insty.domain.notification.listener;

import insty.domain.notification.event.CommunityAnswerCreatedEvent;
import insty.domain.notification.event.CommunityQuestionCreatedEvent;
import insty.domain.notification.event.MentionCreatedEvent;
import insty.domain.notification.implement.AnswerNotificationService;
import insty.domain.notification.implement.MentionNotificationService;
import insty.domain.notification.implement.QuestionNotificationService;
import insty.error.NotificationErrorCode;
import insty.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationMailListener {

    private final QuestionNotificationService questionNotificationService;
    private final AnswerNotificationService answerNotificationService;
    private final MentionNotificationService mentionNotificationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAnswerCreated(CommunityAnswerCreatedEvent event) {
        try {
            answerNotificationService.sendAnswerNotification(event.question(), event.answer());
        } catch (Exception e) {
            throw new CustomException(NotificationErrorCode.ANSWER_NOTIFICATION_FAILED);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onQuestionCreated(CommunityQuestionCreatedEvent event) {
        try {
            questionNotificationService.sendQuestionNotificationToCreator(event.question());
        } catch (Exception e) {
            throw new CustomException(NotificationErrorCode.QUESTION_NOTIFICATION_FAILED);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMentionCreated(MentionCreatedEvent event) {
        try {
            mentionNotificationService.sendMentionNotification(event.mentions(), event.questionTitle());
        } catch (Exception e) {
            throw new CustomException(NotificationErrorCode.MENTION_NOTIFICATION_FAILED);
        }
    }
}
