package insty.model.course;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import insty.error.CourseErrorCode;
import insty.exception.CustomException;
import insty.model.course.fixture.CourseFixtureBuilder;
import insty.model.course.id.CourseTagId;
import insty.model.tag.Tags;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

@Tag("unit")
class CourseTagsTest {

    @Test
    void create_정상() {
        // given
        Course course = CourseFixtureBuilder.getCourse();
        ReflectionTestUtils.setField(course, "id", 1L);
        Tags tags = Tags.create("태그 이름");
        ReflectionTestUtils.setField(tags, "id", 1L);

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
        Tags tags = Tags.create("태그 이름");

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
        Course course = Course.create("제목", "설명", 10000, "강의 추천 대상자", true);
        Tags tags = null;

        // when

        // then
        assertThatThrownBy(() -> CourseTag.create(course, tags))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CourseErrorCode.COURSE_CREATE_ERROR);
    }
}
