package insty.domain.course.implement;

import static insty.domain.common.constant.ViewCountConstants.COURSE_VIEW_DUPLICATE_DURATION;
import static insty.redis.constant.RedisConstants.COURSE_VIEW_PREFIX;

import insty.domain.course.dto.CourseViewContext;
import insty.domain.course.repository.CourseRepository;
import insty.domain.user.repository.UserRepository;
import insty.model.user.UserType;
import insty.redis.adapter.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CourseCounter {

    private final RedisService redisService;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    public void increaseCourseViewCount(Long courseId, CourseViewContext ctx) {
        if (!allowIncrease(courseId, ctx)) return;
        courseRepository.incrementViewCount(courseId);
    }

    public boolean allowIncrease(Long courseId, CourseViewContext ctx) {
        if (ctx == null) return true;

        return userRepository.findById(ctx.userId())
                .map(user -> {
                    if (user.getUserType() == UserType.CREATOR) return false;
                    return saveViewEventSafely(user.getId(), courseId);
                })
                .orElse(true);
    }

    private boolean saveViewEventSafely(Long userId, Long courseId) {
        try {
            return redisService.saveIfAbsent(
                    COURSE_VIEW_PREFIX + userId + ":course:" + courseId,
                    "1",
                    COURSE_VIEW_DUPLICATE_DURATION
            );
        } catch (RuntimeException ex) {
            return true;
        }
    }
}
