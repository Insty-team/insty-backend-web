package insty.domain.auth.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import insty.domain.auth.implement.emailverification.EmailVerificationReader;
import insty.domain.auth.implement.emailverification.EmailVerificationWriter;
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

    @InjectMocks
    private EmailVerificationService emailVerificationService;

    @Test
    void 이메일_인증_정보가_없을때_인증메일을_전송하면_새로운_이메일_인증_정보로_전송한다() {
        // given
        // when
        emailVerificationService.sendVerification("email@email.com");

        // then
        verify(emailVerificationWriter).sendVerification(any(EmailVerification.class));
    }

    @Test
    void 이메일_인증_정보가_있을_때_인증메일을_전송하면_기존_정보로_재발급하고_전송한다() {
        // given
        EmailVerification mock = mock(EmailVerification.class);
        when(emailVerificationReader.findByEmail(anyString())).thenReturn(Optional.of(mock));

        // when
        emailVerificationService.sendVerification("email@email.com");

        // then
        verify(mock).reissue(any(TokenGenerator.class));
        verify(emailVerificationWriter).sendVerification(any(EmailVerification.class));
    }
}