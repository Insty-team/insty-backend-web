package insty.domain.notification.handler;

import insty.domain.notification.common.NotificationUtils;
import insty.domain.notification.event.NewCommunityQuestionEvent;
import insty.domain.notification.publisher.NotificationEventPublisher;
import insty.domain.notification.validation.NewQuestionNotificationValidator;
import insty.mail.MailType;
import insty.mail.event.MailSendEvent;
import insty.mail.payload.CommunityQuestionMailPayload;
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
public class NewQuestionNotificationHandler {

    private final ApplicationEventPublisher eventPublisher;
    private final NotificationEventPublisher notificationEventPublisher;
    private final NotificationUtils notificationUtils;
    private final NewQuestionNotificationValidator notificationValidator;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMailEventHandler(NewCommunityQuestionEvent event) {
        try {
            if (!notificationValidator.validateUserNotification(event.receiver())) {
                return;
            }

            String questionUrl = generateQuestionUrl(event.question().getId());
            String truncatedContent = notificationUtils.truncateContent(
                    event.question().getContent(),
                    notificationUtils.getDefaultPreviewLength()
            );

            // 1. 메일 발송 이벤트 발행
            CommunityQuestionMailPayload mailPayload = new CommunityQuestionMailPayload(
                    event.receiver().getEmail(),
                    event.question().getTitle(),
                    truncatedContent,
                    event.questionAuthor().getNickname(),
                    event.course().getTitle(),
                    questionUrl
            );
            eventPublisher.publishEvent(new MailSendEvent(MailType.COMMUNITY_QUESTION, mailPayload));

            // 2. 알림 저장 이벤트 발행
            notificationEventPublisher.publish(
                    event.receiver().getId(),
                    "NEW_COMMUNITY_QUESTION",
                    "새로운 질문이 등록되었습니다",
                    String.format("%s님이 '%s' 강의에 질문을 남겼습니다",
                            event.questionAuthor().getNickname(), event.course().getTitle()),
                    questionUrl
            );

            log.info("NewQuestionNotification 이벤트 발행 완료: {}", event.receiver().getEmail());
        } catch (Exception e) {
            log.error("NewQuestionNotificationHandler 에러", e);
        }
    }

    private String generateQuestionUrl(Long questionId) {
        return String.format("%s/community/questions/%d", notificationUtils.getDomain(), questionId);
    }

}
