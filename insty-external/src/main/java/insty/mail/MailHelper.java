package insty.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Component
@RequiredArgsConstructor
@Slf4j
public class MailHelper {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Async("mailTaskExecutor")
    @Retryable(
        retryFor = {EmailSendException.class},
        backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public void sendVerificationCode(MailContent content) {
        try {
            MimeMessage message = getMimeMessage(content.to(), content.mailType(), content.variables());
            mailSender.send(message);
        } catch (MailException | MessagingException e) {
            throw new EmailSendException(e);
        }
    }

    @Recover
    public void recover(EmailSendException ex, String to, String subject, String code) {
        log.error("이메일 발송 최종 실패 - 3번 시도 후 포기. email: {}", to, ex);
        // 나중에 슬랙 알림 등 추가되면 추가하기
    }

    private MimeMessage getMimeMessage(
        String to,
        MailType type,
        Map<String, Object> variables
    ) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(to);
        helper.setSubject(type.getSubject());
        helper.setText(getEmailContent(type.getTemplate(), variables), true);

        return message;
    }

    private String getEmailContent(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        variables.forEach(context::setVariable);
        return templateEngine.process(templateName, context);
    }
}