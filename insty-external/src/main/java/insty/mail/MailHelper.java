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

    private static final String DEFAULT_TEMPLATE = "default-email-template";

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Async("mailTaskExecutor")
    @Retryable(
        retryFor = {EmailSendException.class},
        backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public void send(MailContent content) {
        try {
            MailType mailType = content.mailType();
            String emailText = extractEmailText(mailType, content.variables());
            MimeMessage message = getMimeMessage(content.to(), mailType.getSubject(), emailText);
            mailSender.send(message);
        } catch (MailException | MessagingException e) {
            throw new EmailSendException(e);
        }
    }

    @Recover
    public void recover(EmailSendException ex, MailContent content) {
        log.error("이메일 발송 최종 실패 - 3번 시도 후 포기. email: {}, type: {}", content.to(), content.mailType(), ex);
        // 나중에 슬랙 알림 등 추가되면 추가하기
    }

    private MimeMessage getMimeMessage(String to, String subject, String text) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text, true);

        return message;
    }

    private String extractEmailText(MailType mailType, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariable("allVariables", variables);
        variables.forEach(context::setVariable);

        if (mailType.hasTemplate()) {
            return templateEngine.process(mailType.getTemplate(), context);
        }
        return templateEngine.process(DEFAULT_TEMPLATE, context);
    }
}