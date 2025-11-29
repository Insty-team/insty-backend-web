package insty.domain.course.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import insty.domain.common.ViewCountPolicy;
import insty.domain.course.repository.CourseRepository;
import insty.model.course.Course;
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
    private CourseViewCountLimiter courseViewCountLimiter;

    @Test
    void increaseViewCountAndGetCourse_정상() {
        // given
        Long courseId = 1L;
        Long userId = 1L;

        // mock
        when(courseRepository.findById(courseId))
                .thenReturn(Optional.of(mock(Course.class)));
        when(courseViewCountLimiter.allowIncrease(courseId, userId))
                .thenReturn(true);

        // when
        Course course = courseCounter.increaseViewCountAndGetCourse(courseId, userId, ViewCountPolicy.INCREASE);

        // then
        assertThat(course).isNotNull();
        // 실제 증가되었는지 확인 x
    }
}
