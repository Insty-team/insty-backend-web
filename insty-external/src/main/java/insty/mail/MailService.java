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
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Locale;
import java.util.Map;

/**
 * 메일 전송 서비스
 * 템플릿 기반 메일 전송 지원 (신규 방식)
 * 기존 MailBuilder 방식도 하위 호환성을 위해 유지 (구 방식)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    private final JavaMailSender mailSender;
    private final MailFactory mailFactory;
    private final SpringTemplateEngine templateEngine;

    /**
     * 템플릿 기반 메일 전송 (신규 방식)
     * 템플릿 이름과 컨텍스트 맵을 받아서 직접 렌더링 후 전송
     *
     * @param to 수신자 이메일
     * @param subject 메일 제목
     * @param templateName 템플릿 이름 (확장자 제외)
     * @param context 템플릿에 전달할 변수 맵
     */
    @Async("mailTaskExecutor")
    @Retryable(
            retryFor = {EmailSendException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public void sendTemplatedMail(String to, String subject, String templateName, Map<String, Object> context) {
        try {
            // Thymeleaf 컨텍스트 생성
            Context thymeleafContext = new Context(Locale.KOREAN);
            thymeleafContext.setVariables(context);

            // 템플릿 렌더링
            String htmlBody = templateEngine.process(templateName, thymeleafContext);

            // 메일 전송
            MimeMessage message = createMimeMessage(to, subject, htmlBody);
            mailSender.send(message);

            log.info("메일 전송 성공 (템플릿) - template: {}, recipient: {}", templateName, to);
        } catch (MailException | MessagingException e) {
            log.error("메일 전송 실패 (템플릿) - template: {}, recipient: {}", templateName, to, e);
            throw new EmailSendException(e);
        }
    }

    /**
     * 메일 전송 (구 방식 - 하위 호환성)
     * MailBuilder 전략 패턴을 사용하여 메일 타입별로 제목과 본문을 생성
     *
     * @param type    메일 타입
     * @param payload 메일 Payload
     * @param <T>     Payload 타입
     */
    @Async("mailTaskExecutor")
    @Retryable(
            retryFor = {EmailSendException.class},
            maxAttempts = 3,
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
    public void recoverTemplatedMail(EmailSendException ex, String to, String subject, String templateName, Map<String, Object> context) {
        log.error("이메일 발송 최종 실패 (템플릿) - 3번 시도 후 포기. template: {}, recipient: {}", templateName, to, ex);
        // TODO: 슬랙 알림 또는 모니터링 시스템에 전송
    }

    @Recover
    public <T extends MailPayload> void recoverOldMail(EmailSendException ex, MailType type, T payload) {
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
