package insty.model.course;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import insty.error.CourseErrorCode;
import insty.exception.CustomException;
import insty.model.course.fixture.CourseFixtureBuilder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class CourseInstallEnvChecklistTest {

    @Test
    void create_정상() {
        // given
        Course course = CourseFixtureBuilder.getCourse();
        String content = "설치 환경 체크 항목";
        boolean isSupported = true;

        // when
        CourseInstallEnvChecklist checklist = CourseInstallEnvChecklist.create(course, content, isSupported);

        // then
        assertThat(checklist).isNotNull();
        assertThat(checklist.getCourse()).isEqualTo(course);
        assertThat(checklist.getContent()).isEqualTo(content);
        assertThat(checklist.isSupported()).isEqualTo(isSupported);
    }

    @Test
    void create_에러_course가_null이다() {
        // given
        Course course = null;
        String content = "내용";
        boolean isSupported = true;

        // when

        // then
        assertThatThrownBy(() -> CourseInstallEnvChecklist.create(course, content, isSupported))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CourseErrorCode.COURSE_CREATE_ERROR);
    }

    @Test
    void create_에러_content가_null이다() {
        // given
        Course course = Course.create("제목", "설명", 10000, "강의 추천 대상자", true);
        String content = null;
        boolean isSupported = true;

        // when

        // then
        assertThatThrownBy(() -> CourseInstallEnvChecklist.create(course, content, isSupported))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CourseErrorCode.COURSE_CREATE_ERROR);
    }

    @Test
    void create_에러_content에_공백만_있다() {
        // given
        Course course = Course.create("제목", "설명", 10000, "강의 추천 대상자", true);
        String content = "  \n\t\r";
        boolean isSupported = true;

        // when

        // then
        assertThatThrownBy(() -> CourseInstallEnvChecklist.create(course, content, isSupported))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CourseErrorCode.COURSE_CREATE_ERROR);
    }
}
