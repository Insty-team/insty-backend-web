package insty.domain.auth.implement.passwordreset;

import insty.domain.auth.util.StringObjectMapper;

import insty.model.auth.PasswordResetVerification;
import insty.redis.adapter.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class PasswordResetWriter {

    private static final Duration TTL = Duration.ofMinutes(5);
    private final RedisService redisService;

    public void save(PasswordResetVerification verification) {
        String jsonValue = StringObjectMapper.toJson(verification);
        redisService.save(
                "pw-reset:" +verification.getEmail(),
                jsonValue,
                TTL);
    }
}
