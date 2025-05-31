package insty.model.course;

import static org.assertj.core.api.Assertions.assertThat;

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
}
