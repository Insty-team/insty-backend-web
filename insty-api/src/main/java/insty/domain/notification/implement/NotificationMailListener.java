package insty.domain.notification.implement;

import insty.domain.community.implement.CommunityAnswerReader;
import insty.domain.community.implement.CommunityQuestionReader;
import insty.domain.mention.implement.MentionReader;
import insty.domain.notification.event.CommunityAnswerCreatedEvent;
import insty.domain.notification.event.CommunityQuestionCreatedEvent;
import insty.domain.notification.event.MentionCreatedEvent;
import insty.model.mention.Mention;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationMailListener {

    private final NotificationMailService notificationMailService;
    private final CommunityQuestionReader communityQuestionReader;
    private final CommunityAnswerReader communityAnswerReader;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAnswerCreated(CommunityAnswerCreatedEvent event) {
        try {
            var question = communityQuestionReader.getCommunityQuestionWithFilesById(event.questionId());
            var answer = communityAnswerReader.getCommunityAnswerById(event.answerId());
            notificationMailService.sendAnswerNotification(question, answer);
        } catch (Exception e) {
            log.error("답변 알림 메일 발송 실패 - 질문 ID: {}, 답변 ID: {}", event.questionId(), event.answerId(), e);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onQuestionCreated(CommunityQuestionCreatedEvent event) {
        try {
            var question = communityQuestionReader.getCommunityQuestionWithFilesById(event.questionId());
            notificationMailService.sendQuestionNotificationToCreator(question);
        } catch (Exception e) {
            log.error("질문 알림 메일 발송 실패 - 질문 ID: {}", event.questionId(), e);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMentionCreated(MentionCreatedEvent event) {
        try {
            notificationMailService.sendMentionNotification(event.mentions(), event.questionTitle());
        } catch (Exception e) {
            log.error("멘션 알림 메일 발송 실패 - 멘션 개수: {}", event.mentions().size(), e);
        }
    }
}
