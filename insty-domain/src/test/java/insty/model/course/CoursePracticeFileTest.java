package insty.model.course;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import insty.error.CourseErrorCode;
import insty.exception.CustomException;
import insty.model.course.id.CoursePracticeFileId;
import insty.model.file.File;
import insty.model.file.FileFixtureBuilder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class CoursePracticeFileTest {

    @Test
    void create_정상() {
        // given
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();
        File practiceFile = FileFixtureBuilder.getCoursePracticeFileWithId();

        // when
        CoursePracticeFile coursePracticeFile = CoursePracticeFile.create(course, practiceFile);

        // then
        assertThat(coursePracticeFile).isNotNull();
        assertThat(coursePracticeFile.getCourse()).isEqualTo(course);
        assertThat(coursePracticeFile.getPracticeFile()).isEqualTo(practiceFile);
        assertThat(coursePracticeFile.getCoursePracticeFileId()).isEqualTo(
                CoursePracticeFileId.create(course.getId(), practiceFile.getId()));
    }

    @Test
    void create_에러_course가_null이다() {
        // given
        Course course = null;
        File practiceFile = FileFixtureBuilder.getCoursePracticeFileWithId();

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
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();
        File practiceFile = null;

        // when

        // then
        assertThatThrownBy(() -> CoursePracticeFile.create(course, practiceFile))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CourseErrorCode.COURSE_CREATE_ERROR);
    }
}