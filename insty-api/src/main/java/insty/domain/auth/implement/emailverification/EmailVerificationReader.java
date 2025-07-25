package insty.domain.auth.implement.emailverification;

import insty.domain.auth.util.StringObjectMapper;
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

    public Optional<EmailVerification> findByEmail(String email) {
        return redisService.find(email)
            .map(jsonString -> StringObjectMapper.fromJson(jsonString, EmailVerification.class));
    }
}
