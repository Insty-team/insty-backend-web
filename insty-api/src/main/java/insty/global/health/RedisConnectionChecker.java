package insty.global.health;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class RedisConnectionChecker {

    private final StringRedisTemplate redisTemplate;

    @PostConstruct
    public void checkConnection() {
        try {
            redisTemplate.getConnectionFactory().getConnection().ping();
            log.info("레디스 연결 완료");
        } catch (Exception e) {
            log.error("레디스 연결 실패", e);
        }
    }
}
