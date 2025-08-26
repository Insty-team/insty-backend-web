package insty.domain.notification.handler;

import insty.domain.notification.common.NotificationUtils;
import insty.domain.notification.content.NewAnswerMailContent;
import insty.domain.notification.event.NewAnswerNotificationEvent;
import insty.domain.notification.validation.NewAnswerNotificationValidator;
import insty.mail.MailHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class NewAnswerNotificationHandler {

    private final MailHelper mailHelper;
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
            NewAnswerMailContent mailContent = NewAnswerMailContent.of(
                    event.receiver().getEmail(),
                    event.question().getTitle(),
                    notificationUtils.truncateContent(event.answer().getContent(), notificationUtils.getDefaultPreviewLength()),
                    event.answerAuthor().getNickname(),
                    questionUrl
            );

            mailHelper.send(mailContent);
            log.info("NewAnswerNotificationHandler 메일 전송 완료: {}", event.receiver().getEmail());

        } catch (Exception e) {
            log.error("NewAnswerNotificationHandler 에러", e);
            // TODO: observability 시스템(예: Sentry/CloudWatch)에 전송 고려
        }
    }

    private String generateQuestionUrl(Long questionId) {
        return String.format("%s/community/questions/%d", notificationUtils.getDomain(), questionId);
    }
}
