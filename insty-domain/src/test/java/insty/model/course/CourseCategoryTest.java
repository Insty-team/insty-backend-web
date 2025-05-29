package insty.model.course;

import static org.assertj.core.api.Assertions.assertThat;

import insty.model.category.Category;
import insty.model.course.id.CourseCategoryId;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class CourseCategoryTest {

    @Test
    void create_정상() {
        // given
        Course course = Course.create("제목", "설명", 10000, "대상자", null);
        Category category = Category.create(null, 0, "카테고리 이름", 1);

        // when
        CourseCategory courseCategory = CourseCategory.create(course, category);

        // then
        assertThat(courseCategory).isNotNull();
        assertThat(courseCategory.getCourse()).isEqualTo(course);
        assertThat(courseCategory.getCategory()).isEqualTo(category);
        assertThat(courseCategory.getCourseCategoryId()).isEqualTo(
                CourseCategoryId.create(course.getId(), category.getId())
        );
    }
}
