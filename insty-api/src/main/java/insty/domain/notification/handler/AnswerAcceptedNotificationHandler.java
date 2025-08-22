package insty.domain.notification.handler;

import insty.domain.notification.common.NotificationUtils;
import insty.domain.notification.content.CommunityAnswerAcceptMailContent;
import insty.domain.notification.event.AnswerAcceptedNotificationEvent;
import insty.domain.notification.validation.AnswerAcceptedNotificationValidator;
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
public class AnswerAcceptedNotificationHandler {

    private final MailHelper mailHelper;
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
            CommunityAnswerAcceptMailContent mailContent = CommunityAnswerAcceptMailContent.of(
                    event.receiver().getEmail(),
                    event.question().getTitle(),
                    event.answer().getContent(),
                    event.answer().getUser().getNickname(),
                    event.questionAuthor().getNickname(),
                    questionUrl
            );
            
            mailHelper.send(mailContent);
            log.info("AnswerAcceptedNotificationHandler 메일 전송 완료: {}", event.receiver().getEmail());

        } catch (Exception e) {
            log.error("AnswerAcceptedNotificationHandler 에러", e);
            // TODO: observability 시스템(예: Sentry/CloudWatch)에 전송 고려
        }
    }
    
    private String generateQuestionUrl(Long questionId) {
        return String.format("%s/community/questions/%d", notificationUtils.getDomain(), questionId);
    }
}
