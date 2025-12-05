package insty.domain.auth.implement.passwordreset;

import insty.domain.auth.util.StringObjectMapper;
import insty.error.AuthErrorCode;
import insty.exception.CustomException;
import insty.model.auth.PasswordResetVerification;
import insty.redis.adapter.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PasswordResetReader {

    private static final String PASSWORD_HEADER_PREFIX = "pw-reset:";
    private final RedisService redisService;

    public Optional<PasswordResetVerification> findOptionalByEmail(String email){
        return redisService.find(PASSWORD_HEADER_PREFIX + email)
                .map(json -> StringObjectMapper.fromJson(json, PasswordResetVerification.class));
    }

    public PasswordResetVerification findByEmail(String email) {
        return findOptionalByEmail(email)
                .orElseThrow(() -> new CustomException(AuthErrorCode.REQUIRES_EMAIL_VERIFICATION_REQUEST));
    }

    public boolean isVerified(String email) {
        return findOptionalByEmail(email)
                .map(PasswordResetVerification::isVerified)
                .orElse(false);
    }
}
