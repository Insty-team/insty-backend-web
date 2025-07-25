package insty.domain.auth.service;

import insty.domain.auth.implement.emailverification.EmailVerificationReader;
import insty.domain.auth.implement.emailverification.EmailVerificationWriter;
import insty.model.auth.EmailVerification;
import insty.model.auth.SimpleTokenGenerator;
import insty.model.auth.TokenGenerator;
import jakarta.validation.Valid;
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
    private final TokenGenerator tokenGenerator = new SimpleTokenGenerator();

    @Transactional
    public void sendVerification(@NotNull String email) {
        EmailVerification emailVerification = emailVerificationReader.findByEmail(email)
            .map(present -> {
                present.reissue(tokenGenerator);
                return present;
            })
            .orElse(EmailVerification.create(email, tokenGenerator));

        emailVerificationWriter.sendVerification(emailVerification);
    }
}
