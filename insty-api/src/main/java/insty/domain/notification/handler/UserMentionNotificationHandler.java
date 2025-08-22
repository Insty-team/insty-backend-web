package insty.domain.notification.handler;


import insty.domain.notification.common.NotificationUtils;
import insty.domain.notification.content.UserMentionMailContent;
import insty.domain.notification.event.UserMentionedEvent;
import insty.domain.notification.validation.UserMentionNotificationValidator;
import insty.error.NotificationErrorCode;
import insty.exception.CustomException;
import insty.mail.MailHelper;
import insty.model.community.CommunityQuestion;
import insty.model.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserMentionNotificationHandler {

    private final MailHelper mailHelper;
    private final NotificationUtils notificationUtils;
    private final UserMentionNotificationValidator notificationValidator;


    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMailEventHandler(UserMentionedEvent event) {
        try {
            User receiverUser = event.receiver();
            User senderUser = event.sender();
            CommunityQuestion question = event.question();

            if (!notificationValidator.validateUserNotification(receiverUser)) {
                return;
            }
            String email = receiverUser.getEmail();
            String questionTitle = question.getTitle();
            String mentionerName = senderUser.getNickname();
            String questionUrl = generateQuestionUrl(question.getId());
            UserMentionMailContent mailContent = UserMentionMailContent.of(
                    email,
                    questionTitle,
                    mentionerName,
                    questionUrl
            );

            mailHelper.send(mailContent);
            log.info("UserMentionNotificationHandler 메일 전송 완료: receiverId={}", receiverUser.getId());

        } catch (Exception e) {
            log.error("UserMentionNotificationHandler 에러", e);
            // TODO: observability 시스템(예: Sentry/CloudWatch)에 전송 고려
        }
    }

    private String generateQuestionUrl(Long questionId) {
        return String.format("%s/community/questions/%d", notificationUtils.getDomain(), questionId);
    }
}
