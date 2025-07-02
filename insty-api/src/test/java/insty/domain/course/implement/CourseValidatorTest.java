package insty.domain.course.implement;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
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
import org.springframework.web.multipart.MultipartFile;

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
                .isEqualTo(CourseErrorCode.COURSE_CANT_CHANGE);
    }

    @Test
    void validateCourseThumbnailExtension_정상() {
        // given
        MultipartFile thumbnail = mock(MultipartFile.class);

        // mock
        when(thumbnail.getContentType())
                .thenReturn("image/jpeg");

        // when

        // then
        assertThatCode(() -> courseValidator.validateCourseThumbnailExtension(thumbnail))
                .doesNotThrowAnyException();
    }

    @Test
    void validateCourseThumbnailExtension_정상_빈_파일() {
        // given
        MultipartFile thumbnail = null;

        // when

        // then
        assertThatCode(() -> courseValidator.validateCourseThumbnailExtension(thumbnail))
                .doesNotThrowAnyException();
    }

    @Test
    void validateCourseThumbnailExtension_에러_타입_정보가_없다() {
        // given
        MultipartFile thumbnail = mock(MultipartFile.class);

        // when

        // then
        assertThatThrownBy(() -> courseValidator.validateCourseThumbnailExtension(thumbnail))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CourseErrorCode.COURSE_THUMBNAIL_INVALID_EXTENSION);
    }

    @Test
    void validateCourseThumbnailExtension_에러_허용되지_않은_타입이다() {
        // given
        MultipartFile thumbnail = mock(MultipartFile.class);

        // mock
        when(thumbnail.getContentType())
                .thenReturn("image/gif");

        // when

        // then
        assertThatThrownBy(() -> courseValidator.validateCourseThumbnailExtension(thumbnail))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CourseErrorCode.COURSE_THUMBNAIL_INVALID_EXTENSION);
    }
}