package insty.domain.notification.handler;

import insty.mail.MailService;
import insty.mail.event.MailSendEvent;
import insty.mail.payload.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 메일 발송 이벤트 핸들러
 * MailSendEvent를 수신하여 타입별로 메일 발송
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MailEventHandler {

    private final MailService mailService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(MailSendEvent event) {
        try {
            switch (event.type()) {
                case AUTH -> mailService.sendMail(event.type(), (AuthMailPayload) event.payload());
                case COMMUNITY_QUESTION -> mailService.sendMail(event.type(), (CommunityQuestionMailPayload) event.payload());
                case COMMUNITY_ANSWER -> mailService.sendMail(event.type(), (NewAnswerMailPayload) event.payload());
                case COMMUNITY_ANSWER_ACCEPT -> mailService.sendMail(event.type(), (AnswerAcceptMailPayload) event.payload());
                case MENTION -> mailService.sendMail(event.type(), (MentionMailPayload) event.payload());
            }
        } catch (Exception e) {
            log.error("메일 발송 이벤트 처리 실패 - type: {}, recipient: {}", event.type(), event.payload().getRecipient(), e);
        }
    }
}
