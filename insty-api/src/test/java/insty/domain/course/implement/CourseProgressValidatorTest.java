package insty.domain.course.implement;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import insty.domain.course.repository.CourseProgressRepository;
import insty.error.CourseProgressErrorCode;
import insty.exception.CustomException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class CourseProgressValidatorTest {

    @InjectMocks
    private CourseProgressValidator  courseProgressValidator;

    @Mock
    private CourseProgressRepository courseProgressRepository;


    @Test
    void validateCourseProgressExist_정상(){
        //given
        Long  courseId = 1L;
        Long  userId = 1L;

        //mock
        when(courseProgressRepository.existsByUserIdAndCourseId(userId,courseId))
        .thenReturn(false);
        //when

        //then
        assertThatCode(() -> courseProgressValidator.validateCourseProgressNotExists(courseId, userId))
                .doesNotThrowAnyException();

    }

    @Test
    void validateCourseProgressExist_중복강의수강요청(){
        //given
        Long  courseId = 1L;
        Long  userId = 1L;

        //mock
        when(courseProgressRepository.existsByUserIdAndCourseId(userId,courseId))
                .thenReturn(true);
        //when

        //then
        assertThatThrownBy(() -> courseProgressValidator.validateCourseProgressNotExists(courseId, userId))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CourseProgressErrorCode.COURSE_PROGRESS_DUPLICATE);

    }
}