package insty.domain.auth.implement.emailverification;

import insty.domain.auth.util.StringObjectMapper;
import insty.error.AuthErrorCode;
import insty.exception.CustomException;
import insty.model.auth.EmailVerification;
import insty.redis.adapter.RedisService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailVerificationReader {

    private final RedisService redisService;

    public Optional<EmailVerification> findOptionalByEmail(String email) {
        return redisService.find(email)
            .map(jsonString -> StringObjectMapper.fromJson(jsonString, EmailVerification.class));
    }

    public EmailVerification findByEmail(String email) {
        return findOptionalByEmail(email)
            .orElseThrow(() -> new CustomException(AuthErrorCode.REQUIRES_EMAIL_VERIFICATION_REQUEST));
    }

    public boolean checkEmailVerified(String email) {
        return redisService.find(email)
            .map(jsonString -> StringObjectMapper.fromJson(jsonString, EmailVerification.class))
            .map(EmailVerification::isVerified)
            .orElse(false);
    }
}
