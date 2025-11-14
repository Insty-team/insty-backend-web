package insty.mail;

import insty.mail.builder.MailBuilder;
import insty.mail.builder.MailFactory;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 메일 전송 서비스
 * MailBuilder 전략 패턴을 사용하여 메일 타입별로 제목과 본문을 생성
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    private final JavaMailSender mailSender;
    private final MailFactory mailFactory;

    /**
     * 메일 전송 (비동기, 재시도 3회)
     *
     * @param type    메일 타입
     * @param payload 메일 Payload
     * @param <T>     Payload 타입
     */
    @Async("mailTaskExecutor")
    @Retryable(
            retryFor = {EmailSendException.class},
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public <T extends MailPayload> void sendMail(MailType type, T payload) {
        try {
            MailBuilder<T> builder = mailFactory.getBuilder(type);
            String subject = builder.buildSubject(payload);
            String body = builder.buildBody(payload);

            MimeMessage message = createMimeMessage(payload.getRecipient(), subject, body);
            mailSender.send(message);

            log.info("메일 전송 성공 - type: {}, recipient: {}", type, payload.getRecipient());
        } catch (MailException | MessagingException e) {
            log.error("메일 전송 실패 - type: {}, recipient: {}", type, payload.getRecipient(), e);
            throw new EmailSendException(e);
        }
    }

    @Recover
    public <T extends MailPayload> void recover(EmailSendException ex, MailType type, T payload) {
        log.error("이메일 발송 최종 실패 - 3번 시도 후 포기. type: {}, recipient: {}", type, payload.getRecipient(), ex);
        // TODO: 슬랙 알림 또는 모니터링 시스템에 전송
    }

    private MimeMessage createMimeMessage(String to, String subject, String htmlBody) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);

        return message;
    }
}
