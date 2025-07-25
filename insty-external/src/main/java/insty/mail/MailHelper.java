package insty.mail;

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
    public void sendVerificationCode(String to, String subject, String code) {
        try {
            MimeMessage message = getMimeMessage(to, subject, code);
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

    private MimeMessage getMimeMessage(String to, String subject, String code) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(getEmailContent(code), true);

        return message;
    }

    private String getEmailContent(String code) {
        Context context = new Context();
        context.setVariable("code", code);
        return templateEngine.process("email", context);
    }
}