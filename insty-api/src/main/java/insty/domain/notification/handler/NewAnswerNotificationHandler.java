package insty.domain.notification.handler;

import insty.domain.notification.common.NotificationUtils;
import insty.domain.notification.event.NewAnswerNotificationEvent;
import insty.domain.notification.publisher.NotificationEventPublisher;
import insty.domain.notification.validation.NewAnswerNotificationValidator;
import insty.mail.MailType;
import insty.mail.event.MailSendEvent;
import insty.mail.payload.NewAnswerMailPayload;
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
public class NewAnswerNotificationHandler {

    private final ApplicationEventPublisher eventPublisher;
    private final NotificationEventPublisher notificationEventPublisher;
    private final NotificationUtils notificationUtils;
    private final NewAnswerNotificationValidator notificationValidator;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMailEventHandler(NewAnswerNotificationEvent event) {
        try {
            if (!notificationValidator.validateUserNotification(event.receiver())) {
                return;
            }

            String questionUrl = generateQuestionUrl(event.question().getId());
            String truncatedContent = notificationUtils.truncateContent(
                    event.answer().getContent(),
                    notificationUtils.getDefaultPreviewLength()
            );

            // 1. 메일 발송 이벤트 발행
            NewAnswerMailPayload mailPayload = new NewAnswerMailPayload(
                    event.receiver().getEmail(),
                    event.question().getTitle(),
                    truncatedContent,
                    event.answerAuthor().getNickname(),
                    questionUrl
            );
            eventPublisher.publishEvent(new MailSendEvent(MailType.COMMUNITY_ANSWER, mailPayload));

            // 2. 알림 저장 이벤트 발행
            notificationEventPublisher.publish(
                    event.receiver().getId(),
                    "NEW_COMMUNITY_ANSWER",
                    "새로운 답변이 등록되었습니다",
                    String.format("%s님이 회원님의 질문에 답변을 남겼습니다", event.answerAuthor().getNickname()),
                    questionUrl
            );

            log.info("NewAnswerNotification 이벤트 발행 완료: {}", event.receiver().getEmail());

        } catch (Exception e) {
            log.error("NewAnswerNotificationHandler 에러", e);
        }
    }

    private String generateQuestionUrl(Long questionId) {
        return String.format("%s/community/questions/%d", notificationUtils.getDomain(), questionId);
    }
}
