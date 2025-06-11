package insty.model.course;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import insty.error.CourseErrorCode;
import insty.exception.CustomException;
import insty.model.course.fixture.CourseFixtureBuilder;
import insty.model.course.id.CoursePracticeFileId;
import insty.model.file.File;
import insty.model.file.FileContainerType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

@Tag("unit")
class CoursePracticeFileTest {

    @Test
    void create_정상() {
        // given
        Course course = CourseFixtureBuilder.getCourse();
        ReflectionTestUtils.setField(course, "id", 1L);
        File file = File.create(FileContainerType.COURSE_THUMBNAIL, 1L, "00000000-0000-0000-0000-000000000001.jpg",
                "thumb.jpg", "image/jpeg", 10);
        ReflectionTestUtils.setField(file, "id", 1L);

        // when
        CoursePracticeFile coursePracticeFile = CoursePracticeFile.create(course, file);

        // then
        assertThat(coursePracticeFile).isNotNull();
        assertThat(coursePracticeFile.getCourse()).isEqualTo(course);
        assertThat(coursePracticeFile.getPracticeFile()).isEqualTo(file);
        assertThat(coursePracticeFile.getCoursePracticeFileId()).isEqualTo(
                CoursePracticeFileId.create(course.getId(), file.getId()));
    }

    @Test
    void create_에러_course가_null이다() {
        // given
        Course course = null;
        File practiceFile = File.create(FileContainerType.COURSE_THUMBNAIL, 1L,
                "00000000-0000-0000-0000-000000000001.jpg", "thumb.jpg", "image/jpeg", 10);

        // when

        // then
        assertThatThrownBy(() -> CoursePracticeFile.create(course, practiceFile))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CourseErrorCode.COURSE_CREATE_ERROR);
    }

    @Test
    void create_에러_practiceFile이_null이다() {
        // given
        Course course = Course.create("제목", "설명", 10000, "강의 추천 대상자", true);
        File practiceFile = null;

        // when

        // then
        assertThatThrownBy(() -> CoursePracticeFile.create(course, practiceFile))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CourseErrorCode.COURSE_CREATE_ERROR);
    }
}