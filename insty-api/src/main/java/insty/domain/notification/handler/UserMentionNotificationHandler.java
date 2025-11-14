package insty.domain.notification.handler;

import insty.domain.notification.common.NotificationUtils;
import insty.domain.notification.event.UserMentionedEvent;
import insty.domain.notification.publisher.NotificationEventPublisher;
import insty.domain.notification.validation.UserMentionNotificationValidator;
import insty.mail.MailType;
import insty.mail.event.MailSendEvent;
import insty.mail.payload.MentionMailPayload;
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
public class UserMentionNotificationHandler {

    private final ApplicationEventPublisher eventPublisher;
    private final NotificationEventPublisher notificationEventPublisher;
    private final NotificationUtils notificationUtils;
    private final UserMentionNotificationValidator notificationValidator;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMailEventHandler(UserMentionedEvent event) {
        try {
            if (!notificationValidator.validateUserNotification(event.receiver())) {
                return;
            }

            String questionUrl = generateQuestionUrl(event.question().getId());

            // 1. 메일 발송 이벤트 발행
            MentionMailPayload mailPayload = new MentionMailPayload(
                    event.receiver().getEmail(),
                    event.question().getTitle(),
                    event.sender().getNickname(),
                    questionUrl
            );
            eventPublisher.publishEvent(new MailSendEvent(MailType.MENTION, mailPayload));

            // 2. 알림 저장 이벤트 발행
            notificationEventPublisher.publish(
                    event.receiver().getId(),
                    "USER_MENTIONED",
                    "회원님이 멘션되었습니다",
                    String.format("%s님이 회원님을 멘션했습니다", event.sender().getNickname()),
                    questionUrl
            );

            log.info("UserMentionNotification 이벤트 발행 완료: {}", event.receiver().getEmail());

        } catch (Exception e) {
            log.error("UserMentionNotificationHandler 에러", e);
        }
    }

    private String generateQuestionUrl(Long questionId) {
        return String.format("%s/community/questions/%d", notificationUtils.getDomain(), questionId);
    }
}
