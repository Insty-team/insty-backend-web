package insty.model.course.id;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class CourseTagsIdTest {

    @Test
    void create_정상() {
        // given
        Long courseId = 1L;
        Long tagId = 2L;

        // when
        CourseTagId courseTagId = CourseTagId.create(courseId, tagId);

        // then
        assertThat(courseTagId).isNotNull();
        assertThat(courseTagId.getCourseId()).isEqualTo(courseId);
        assertThat(courseTagId.getTagId()).isEqualTo(tagId);
    }

    @Test
    void equals_hashCode_정상() {
        CourseTagId id1 = CourseTagId.create(1L, 2L);
        CourseTagId id2 = CourseTagId.create(1L, 2L);

        assertThat(id1).isEqualTo(id2);
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
    }
}
