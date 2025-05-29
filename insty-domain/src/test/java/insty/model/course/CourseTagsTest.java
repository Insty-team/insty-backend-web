package insty.model.course;

import static org.assertj.core.api.Assertions.assertThat;

import insty.model.course.fixture.CourseFixtureBuilder;
import insty.model.course.id.CourseTagId;
import insty.model.tag.Tags;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class CourseTagsTest {

    @Test
    void create_정상() {
        // given
        Course course = CourseFixtureBuilder.getCourse();
        Tags tags = Tags.create("태그 이름");

        // when
        CourseTag courseTag = CourseTag.create(course, tags);

        // then
        assertThat(courseTag).isNotNull();
        assertThat(courseTag.getCourse()).isEqualTo(course);
        assertThat(courseTag.getTags()).isEqualTo(tags);
        assertThat(courseTag.getCourseTagId()).isEqualTo(CourseTagId.create(course.getId(), tags.getId()));
    }
}
