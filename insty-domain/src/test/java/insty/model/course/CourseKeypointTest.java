package insty.model.course;

import static org.assertj.core.api.Assertions.assertThat;

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
}
