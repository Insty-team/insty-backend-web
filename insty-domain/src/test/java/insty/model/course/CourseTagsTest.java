package insty.model.course;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import insty.error.CourseErrorCode;
import insty.exception.CustomException;
import insty.model.course.id.CourseTagId;
import insty.model.tag.Tags;
import insty.model.tag.TagsFixtureBuilder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class CourseTagsTest {

    @Test
    void create_정상() {
        // given
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();
        Tags tags = TagsFixtureBuilder.getTagsWithId();

        // when
        CourseTag courseTag = CourseTag.create(course, tags);

        // then
        assertThat(courseTag).isNotNull();
        assertThat(courseTag.getCourse()).isEqualTo(course);
        assertThat(courseTag.getTags()).isEqualTo(tags);
        assertThat(courseTag.getCourseTagId()).isEqualTo(CourseTagId.create(course.getId(), tags.getId()));
    }

    @Test
    void create_에러_course가_null이다() {
        // given
        Course course = null;
        Tags tags = TagsFixtureBuilder.getTagsWithId();

        // when

        // then
        assertThatThrownBy(() -> CourseTag.create(course, tags))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CourseErrorCode.COURSE_CREATE_ERROR);
    }

    @Test
    void create_에러_tags가_null이다() {
        // given
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();
        Tags tags = null;

        // when

        // then
        assertThatThrownBy(() -> CourseTag.create(course, tags))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CourseErrorCode.COURSE_CREATE_ERROR);
    }
}
