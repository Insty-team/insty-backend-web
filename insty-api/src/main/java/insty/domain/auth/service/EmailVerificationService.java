package insty.domain.auth.service;

import insty.domain.auth.implement.emailverification.EmailVerificationReader;
import insty.domain.auth.implement.emailverification.EmailVerificationWriter;
import insty.domain.auth.implement.emailverification.MailAuthenticateContent;
import insty.mail.MailHelper;
import insty.model.auth.EmailVerification;
import insty.model.auth.SimpleTokenGenerator;
import insty.model.auth.TokenGenerator;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@RequiredArgsConstructor
@Validated
public class EmailVerificationService {

    private final EmailVerificationWriter emailVerificationWriter;
    private final EmailVerificationReader emailVerificationReader;
    private final MailHelper mailHelper;
    private final TokenGenerator tokenGenerator = new SimpleTokenGenerator();

    @Transactional
    public void sendVerification(@NotNull String email) {
        EmailVerification emailVerification = emailVerificationReader.findOptionalByEmail(email)
            .map(present -> present.reissue(tokenGenerator))
            .orElse(EmailVerification.create(email, tokenGenerator));

        MailAuthenticateContent content = MailAuthenticateContent.of(email, emailVerification.getToken());
        mailHelper.sendVerificationCode(content);
        emailVerificationWriter.save(emailVerification);
    }

    @Transactional
    public void verifyEmailCode(@NotNull String email, @NotNull String code) {
        EmailVerification emailVerification = emailVerificationReader.findByEmail(email);
        emailVerification.verify(code);

        emailVerificationWriter.save(emailVerification);
    }
}
