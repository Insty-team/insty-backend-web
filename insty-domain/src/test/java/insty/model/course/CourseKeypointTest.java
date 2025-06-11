package insty.model.course;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import insty.error.CourseErrorCode;
import insty.exception.CustomException;
import insty.model.course.fixture.CourseFixtureBuilder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class CourseKeypointTest {

    @Test
    void create_정상() {
        // given
        Course course = CourseFixtureBuilder.getCourse();
        String content = "핵심 포인트 설명";

        // when
        CourseKeypoint keypoint = CourseKeypoint.create(course, content);

        // then
        assertThat(keypoint).isNotNull();
        assertThat(keypoint.getCourse()).isEqualTo(course);
        assertThat(keypoint.getContent()).isEqualTo(content);
    }

    @Test
    void create_에러_course가_null이다() {
        // given
        Course course = null;
        String content = "내용";

        // when

        // then
        assertThatThrownBy(() -> CourseKeypoint.create(course, content))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CourseErrorCode.COURSE_CREATE_ERROR);
    }

    @Test
    void create_에러_content가_null이다() {
        // given
        Course course = Course.create("제목", "설명", 10000, "강의 추천 대상자", true);
        String content = null;

        // when

        // then
        assertThatThrownBy(() -> CourseKeypoint.create(course, content))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CourseErrorCode.COURSE_CREATE_ERROR);
    }

    @Test
    void create_에러_content에_공백만_있다() {
        // given
        Course course = Course.create("제목", "설명", 10000, "강의 추천 대상자", true);
        String content = "  \n\t\r";

        // when

        // then
        assertThatThrownBy(() -> CourseKeypoint.create(course, content))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CourseErrorCode.COURSE_CREATE_ERROR);
    }
}
