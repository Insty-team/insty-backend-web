package insty.domain.auth.implement.emailverification;

import insty.domain.auth.util.StringObjectMapper;
import insty.model.auth.EmailVerification;
import insty.redis.adapter.RedisService;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailVerificationWriter {

    private static final Duration EMAIL_VERIFICATION_TIMEOUT = Duration.ofMinutes(5);

    private final RedisService redisService;

    public void save(EmailVerification emailVerification) {
        String jsonValue = StringObjectMapper.toJson(emailVerification);
        redisService.save(emailVerification.getEmail(), jsonValue, EMAIL_VERIFICATION_TIMEOUT);
    }
}
