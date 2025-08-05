package insty.domain.auth.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import insty.domain.auth.implement.emailverification.EmailVerificationReader;
import insty.domain.auth.implement.emailverification.EmailVerificationWriter;
import insty.mail.MailContent;
import insty.mail.MailHelper;
import insty.model.auth.EmailVerification;
import insty.model.auth.TokenGenerator;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    @Mock
    private EmailVerificationWriter emailVerificationWriter;

    @Mock
    private EmailVerificationReader emailVerificationReader;

    @Mock
    private MailHelper mailHelper;

    @InjectMocks
    private EmailVerificationService emailVerificationService;

    @Test
    void 이메일_인증_정보가_없을때_인증메일을_전송하면_새로운_이메일_인증_정보로_전송한다() {
        // given
        // when
        emailVerificationService.sendVerification("email@email.com");

        // then
        verify(mailHelper).send(any(MailContent.class));
        verify(emailVerificationWriter).save(any(EmailVerification.class));
    }

    @Test
    void 이메일_인증_정보가_존재한다면_인증메일을_전송시_인증_정보를_재발급하고_전송한다() {
        // given
        EmailVerification existingVerification = mock(EmailVerification.class);
        when(emailVerificationReader.findOptionalByEmail(anyString())).thenReturn(Optional.of(existingVerification));
        EmailVerification newVerification = mock(EmailVerification.class);
        when(existingVerification.reissue(any(TokenGenerator.class))).thenReturn(newVerification);
        when(newVerification.getToken()).thenReturn("token");

        // when
        emailVerificationService.sendVerification("email@email.com");

        // then
        verify(mailHelper).send(any(MailContent.class));
        verify(emailVerificationWriter).save(newVerification);
    }

    @Test
    void 이메일_정보와_인증_코드가_주어지면_인증_검증을_한다() {
        // given
        EmailVerification existingVerification = mock(EmailVerification.class);
        when(emailVerificationReader.findByEmail(anyString())).thenReturn(existingVerification);

        // when
        emailVerificationService.verifyEmailCode("email@email.com", "code");

        // then
        verify(existingVerification).verify(eq("code"));
        verify(emailVerificationWriter).save(existingVerification);
    }
}