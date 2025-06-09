package insty.domain.course.implement;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import insty.domain.course.repository.CourseRepository;
import insty.error.CourseErrorCode;
import insty.exception.CustomException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class CourseValidatorTest {

    @InjectMocks
    private CourseValidator courseValidator;

    @Mock
    private CourseRepository courseRepository;

    @Test
    void validateCourseOwner_정상() {
        // given
        Long courseId = 1L;
        Long userId = 1L;

        // mock
        when(courseRepository.existsByIdAndUserId(courseId, userId))
                .thenReturn(true);

        // when

        // then
        assertThatCode(() -> courseValidator.validateCourseOwner(courseId, userId))
                .doesNotThrowAnyException();
    }

    @Test
    void validateCourseOwner_에러_강의의_주인이_아님() {
        // given
        Long courseId = 1L;
        Long userId = 1L;

        // mock
        when(courseRepository.existsByIdAndUserId(courseId, userId))
                .thenReturn(false);

        // when

        // then
        assertThatThrownBy(() -> courseValidator.validateCourseOwner(courseId, userId))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CourseErrorCode.COURSE_CANT_DELETE);
    }
}