package insty.domain.course.implement;

import static insty.redis.constant.RedisConstants.COURSE_VIEW_PREFIX;

import insty.redis.adapter.RedisService;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CourseViewCountLimiter {

    private static final Duration VIEW_DUPLICATE_DURATION = Duration.ofHours(1);

    private final RedisService redisService;

    /**
     * 조회수를 증가시켜야 할 경우 true를 반환한다. 동일한 사용자가 1시간 이내에 다시 조회하면 Redis에 남긴 마커를 참고하여 증가를
     * 생략한다.
     */
    public boolean allowIncrease(Long courseId, Long userId) {
        if (userId == null) {
            return true;
        }

        try {
            return redisService.saveIfAbsent(buildKey(userId, courseId), "1", VIEW_DUPLICATE_DURATION);
        } catch (RuntimeException ex) {
            log.warn("Failed to access Redis when handling course view duplication. courseId={}, userId={}", courseId,
                    userId, ex);
            return true;
        }
    }

    private String buildKey(Long userId, Long courseId) {
        return COURSE_VIEW_PREFIX + userId + ":course:" + courseId;
    }
}
