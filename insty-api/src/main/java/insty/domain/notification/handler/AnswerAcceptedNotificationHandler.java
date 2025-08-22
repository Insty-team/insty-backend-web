package insty.domain.notification.handler;

import insty.domain.notification.common.NotificationUtils;
import insty.domain.notification.content.CommunityAnswerAcceptMailContent;
import insty.domain.notification.event.AnswerAcceptedNotificationEvent;
import insty.domain.notification.validation.AnswerAcceptedNotificationValidator;
import insty.error.NotificationErrorCode;
import insty.exception.CustomException;
import insty.mail.MailHelper;
import insty.model.community.CommunityAnswer;
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
public class AnswerAcceptedNotificationHandler {

    private final MailHelper mailHelper;
    private final NotificationUtils notificationUtils;
    private final AnswerAcceptedNotificationValidator notificationValidator;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMailEventHandler(AnswerAcceptedNotificationEvent event) {
        try {
            CommunityQuestion question = event.question();
            CommunityAnswer answer = event.answer();
            User receiverUser = event.receiver();
            User questionAuthor = question.getUser();
            User answerAuthor = answer.getUser();

            if (!notificationValidator.validateUserNotification(receiverUser)) {
                return;
            }

            String questionTitle = question.getTitle();
            String answerContent = answer.getContent();
            String answerAuthorName = answerAuthor.getNickname();
            String questionAuthorName = questionAuthor.getNickname();
            String questionUrl = generateQuestionUrl(question.getId());

            CommunityAnswerAcceptMailContent mailContent = CommunityAnswerAcceptMailContent.of(
                    receiverUser.getEmail(),
                    questionTitle,
                    answerContent,
                    answerAuthorName,
                    questionAuthorName,
                    questionUrl
            );

            mailHelper.send(mailContent);
            log.info("AnswerAcceptedNotificationHandler 메일 전송 완료: {}", receiverUser.getEmail());

        } catch (Exception e) {
            log.error("AnswerAcceptedNotificationHandler 에러", e);
            // TODO: observability 시스템(예: Sentry/CloudWatch)에 전송 고려
        }
    }

    private String generateQuestionUrl(Long questionId) {
        return String.format("%s/community/questions/%d", notificationUtils.getDomain(), questionId);
    }
}
