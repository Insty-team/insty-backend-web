package insty.domain.notification.handler;

import insty.domain.notification.common.NotificationUtils;
import insty.domain.notification.event.AnswerAcceptedNotificationEvent;
import insty.domain.notification.publisher.NotificationEventPublisher;
import insty.domain.notification.validation.AnswerAcceptedNotificationValidator;
import insty.mail.MailType;
import insty.mail.event.MailSendEvent;
import insty.mail.payload.AnswerAcceptMailPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnswerAcceptedNotificationHandler {

    private final ApplicationEventPublisher eventPublisher;
    private final NotificationEventPublisher notificationEventPublisher;
    private final NotificationUtils notificationUtils;
    private final AnswerAcceptedNotificationValidator notificationValidator;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMailEventHandler(AnswerAcceptedNotificationEvent event) {
        try {
            if (!notificationValidator.validateUserNotification(event.receiver())) {
                return;
            }

            String questionUrl = generateQuestionUrl(event.question().getId());

            // 1. 메일 발송 이벤트 발행
            AnswerAcceptMailPayload mailPayload = new AnswerAcceptMailPayload(
                    event.receiver().getEmail(),
                    event.question().getTitle(),
                    event.answer().getContent(),
                    event.answer().getUser().getNickname(),
                    event.questionAuthor().getNickname(),
                    questionUrl
            );
            eventPublisher.publishEvent(new MailSendEvent(MailType.COMMUNITY_ANSWER_ACCEPT, mailPayload));

            // 2. 알림 저장 이벤트 발행
            notificationEventPublisher.publish(
                    event.receiver().getId(),
                    "COMMUNITY_ANSWER_ACCEPT",
                    "답변이 채택되었습니다",
                    String.format("회원님의 답변이 '%s' 질문에서 채택되었습니다", event.question().getTitle()),
                    questionUrl
            );

            log.info("AnswerAcceptedNotification 이벤트 발행 완료: {}", event.receiver().getEmail());

        } catch (Exception e) {
            log.error("AnswerAcceptedNotificationHandler 에러", e);
        }
    }

    private String generateQuestionUrl(Long questionId) {
        return String.format("%s/community/questions/%d", notificationUtils.getDomain(), questionId);
    }
}
