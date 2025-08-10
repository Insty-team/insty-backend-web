package insty.domain.community.implement.mail;

import insty.domain.community.event.CommunityAnswerCreatedEvent;
import insty.domain.community.event.CommunityQuestionCreatedEvent;
import insty.domain.community.implement.CommunityQuestionReader;
import insty.domain.community.implement.CommunityAnswerReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommunityMailListener {

    private final CommunityMailService communityMailService;
    private final CommunityQuestionReader communityQuestionReader;
    private final CommunityAnswerReader communityAnswerReader;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAnswerCreated(CommunityAnswerCreatedEvent event) {
        try {
            var question = communityQuestionReader.getCommunityQuestionWithFilesById(event.questionId());
            var answer = communityAnswerReader.getCommunityAnswerById(event.answerId());
            communityMailService.sendAnswerNotification(question, answer);
        } catch (Exception e) {
            log.error("답변 알림 메일 발송 실패 - 질문 ID: {}, 답변 ID: {}", event.questionId(), event.answerId(), e);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onQuestionCreated(CommunityQuestionCreatedEvent event) {
        try {
            var question = communityQuestionReader.getCommunityQuestionWithFilesById(event.questionId());
            communityMailService.sendQuestionNotificationToCreator(question);
        } catch (Exception e) {
            log.error("질문 알림 메일 발송 실패 - 질문 ID: {}", event.questionId(), e);
        }
    }
}
