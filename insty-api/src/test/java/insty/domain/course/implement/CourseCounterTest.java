package insty.domain.course.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import insty.domain.course.dto.CourseViewContext;
import insty.domain.course.repository.CourseRepository;
import insty.domain.user.repository.UserRepository;
import insty.model.course.Course;
import insty.model.user.User;
import insty.model.user.UserType;
import insty.redis.adapter.RedisService;
import java.util.Optional;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class CourseCounterTest {

    @InjectMocks
    private CourseCounter courseCounter;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RedisService redisService;

    private final Long COURSE_ID = 1L;
    private final Long USER_ID = 999L;

    @Test
    void increaseCourseViewCount_ctxNull_조회수증가() {
        // when
        courseCounter.increaseCourseViewCount(COURSE_ID, null);

        // then
        verify(courseRepository).incrementViewCount(COURSE_ID);
    }

    @Test
    void increaseCourseViewCount_creator_증가안함() {
        // given
        User user = User.create("", "", "");
        user.update(UserType.CREATOR);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        CourseViewContext ctx = CourseViewContext.of(USER_ID);

        // when
        courseCounter.increaseCourseViewCount(COURSE_ID, ctx);

        // then
        verify(courseRepository, never()).incrementViewCount(any());
    }

    @Test
    void increaseCourseViewCount_user이고_redis_true면_증가() {
        // given
        User user = User.create("", "", "");
        user.update(UserType.LEARNER);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(redisService.saveIfAbsent(anyString(), anyString(), any()))
                .thenReturn(true);

        CourseViewContext ctx = CourseViewContext.of(USER_ID);

        // when
        courseCounter.increaseCourseViewCount(COURSE_ID, ctx);

        // then
        verify(courseRepository).incrementViewCount(COURSE_ID);
    }

    @Test
    void increaseCourseViewCount_user이고_redis_false면_증가안함() {
        // given
        User user = User.create("", "", "");
        user.update(UserType.LEARNER);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(redisService.saveIfAbsent(anyString(), anyString(), any()))
                .thenReturn(false); // 중복조회 상황

        CourseViewContext ctx = CourseViewContext.of(USER_ID);

        // when
        courseCounter.increaseCourseViewCount(COURSE_ID, ctx);

        // then
        verify(courseRepository, never()).incrementViewCount(any());
    }

    @Test
    void increaseCourseViewCount_user이고_redis_exception나면_증가() {
        // given
        User user = User.create("", "", "");
        user.update(UserType.LEARNER);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(redisService.saveIfAbsent(anyString(), anyString(), any()))
                .thenThrow(new RuntimeException("redis down"));

        CourseViewContext ctx = CourseViewContext.of(USER_ID);

        // when
        courseCounter.increaseCourseViewCount(COURSE_ID, ctx);

        // then
        verify(courseRepository).incrementViewCount(COURSE_ID);
    }
}
