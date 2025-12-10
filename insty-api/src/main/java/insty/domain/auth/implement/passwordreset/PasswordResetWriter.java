package insty.domain.auth.implement.passwordreset;

import insty.domain.auth.util.StringObjectMapper;

import insty.model.auth.PasswordResetVerification;
import insty.redis.adapter.RedisService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class PasswordResetWriter {

    private static final Duration TTL = Duration.ofMinutes(5);
    private static final String PASSWORD_HEADER_PREFIX = "pw-reset:";
    private final RedisService redisService;

    public void save(PasswordResetVerification verification) {
        String jsonValue = StringObjectMapper.toJson(verification);
        redisService.save(
                PASSWORD_HEADER_PREFIX + verification.getEmail(),
                jsonValue,
                TTL);
    }

    public void deleteByEmail(@NotNull String email) {
        redisService.delete(PASSWORD_HEADER_PREFIX + email);
    }
}
