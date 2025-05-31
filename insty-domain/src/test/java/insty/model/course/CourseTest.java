package insty.model.course;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class CourseTest {

    @Test
    void create_정상() {
        // given
        String title = "제목";
        String description = "설명";
        int price = 10000;
        String targetAudience = "강의 추천 대상자";
        Long thumbnailId = null;
        boolean isShow = true;

        // when
        Course course = Course.create(title, description, price, targetAudience, thumbnailId, isShow);

        // then
        assertThat(course).isNotNull();
        assertThat(course.getTitle()).isEqualTo(title);
        assertThat(course.getDescription()).isEqualTo(description);
        assertThat(course.getPrice()).isEqualTo(price);
        assertThat(course.getViewCount()).isEqualTo(0);
        assertThat(course.getLikeCount()).isEqualTo(0);
        assertThat(course.getTargetAudience()).isEqualTo(targetAudience);
        assertThat(course.getThumbnailId()).isEqualTo(thumbnailId);
        assertThat(course.isShow()).isEqualTo(isShow);
    }
}