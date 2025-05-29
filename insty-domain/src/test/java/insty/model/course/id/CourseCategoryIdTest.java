package insty.model.course.id;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class CourseCategoryIdTest {

    @Test
    void create_정상() {
        // given
        Long courseId = 1L;
        Long categoryId = 2L;

        // when
        CourseCategoryId courseCategoryId = CourseCategoryId.create(courseId, categoryId);

        // then
        assertThat(courseCategoryId).isNotNull();
        assertThat(courseCategoryId.getCourseId()).isEqualTo(courseId);
        assertThat(courseCategoryId.getCategoryId()).isEqualTo(categoryId);
    }

    @Test
    void equals_hashCode_정상() {
        CourseCategoryId id1 = CourseCategoryId.create(1L, 2L);
        CourseCategoryId id2 = CourseCategoryId.create(1L, 2L);

        assertThat(id1).isEqualTo(id2);
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
    }
}