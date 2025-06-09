package insty.model.course;

import static org.assertj.core.api.Assertions.assertThat;

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
}