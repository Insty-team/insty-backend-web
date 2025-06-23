package insty.domain.auth.implement;

import static insty.constants.JwtConstants.REFRESH_TOKEN_VALIDITY;
import static insty.redis.constant.RedisConstants.REDIS_TOKEN_PREFIX;

import insty.redis.adapter.RedisService;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthTokenRedisWriter {

    private final RedisService redisService;

    /**
     * Redis에 refreshtoken:user:{userId} 형태로 값 넣기
     */
    public void saveRefreshToken(Long userId, String refreshToken) {
        redisService.save(REDIS_TOKEN_PREFIX + userId, refreshToken, Duration.ofMillis(REFRESH_TOKEN_VALIDITY));
    }

    /**
     * Redis에 refreshtoken:user:{userId} 형태로 있는 값 삭제
     */
    public void deleteRefreshToken(Long userId) {
        redisService.delete(REDIS_TOKEN_PREFIX + userId);
    }
}
