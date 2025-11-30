package insty.redis.adapter;

import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisService {
    private final RedisTemplate<String, String> redisTemplate;

    // 값 넣기
    public void save(String key, String value, Duration ttl){
        redisTemplate.opsForValue().set(key, value, ttl);
    }

    // 값이 없을 때만 넣기
    public boolean saveIfAbsent(String key, String value, Duration ttl) {
        Boolean result = redisTemplate.opsForValue().setIfAbsent(key, value, ttl);
        return Boolean.TRUE.equals(result);
    }

    // 값 가져오기
    public Optional<String> find(String key){
        return Optional.ofNullable(redisTemplate.opsForValue().get(key));
    }

    // 값 삭제하기
    public void delete(String key){
        redisTemplate.delete(key);
    }

}
