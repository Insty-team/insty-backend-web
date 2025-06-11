package insty.model.course;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import insty.error.CourseErrorCode;
import insty.exception.CustomException;
import insty.model.course.fixture.CourseFixtureBuilder;
import insty.model.file.File;
import insty.model.file.FileContainerType;
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
        Course course = Course.create(title, description, price, targetAudience, isShow);

        // then
        assertThat(course).isNotNull();
        assertThat(course.getTitle()).isEqualTo(title);
        assertThat(course.getDescription()).isEqualTo(description);
        assertThat(course.getPrice()).isEqualTo(price);
        assertThat(course.getViewCount()).isEqualTo(0);
        assertThat(course.getLikeCount()).isEqualTo(0);
        assertThat(course.getTargetAudience()).isEqualTo(targetAudience);
        assertThat(course.isShow()).isEqualTo(isShow);
    }

    @Test
    void create_에러_title이_null이다() {
        // given
        String title = null;
        String description = null;
        int price = 10000;
        String targetAudience = null;
        boolean isShow = true;

        // when

        // then
        assertThatThrownBy(() -> Course.create(title, description, price, targetAudience, isShow))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CourseErrorCode.COURSE_CREATE_ERROR);
    }

    @Test
    void create_에러_title에_공백만_있다() {
        // given
        String title = "     ";
        String description = null;
        int price = 10000;
        String targetAudience = null;
        boolean isShow = true;

        // when

        // then
        assertThatThrownBy(() -> Course.create(title, description, price, targetAudience, isShow))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CourseErrorCode.COURSE_CREATE_ERROR);
    }

    @Test
    void create_에러_price가_0_미만이다() {
        // given
        String title = "제목";
        String description = null;
        int price = -1;
        String targetAudience = null;
        boolean isShow = true;

        // when

        // then
        assertThatThrownBy(() -> Course.create(title, description, price, targetAudience, isShow))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CourseErrorCode.COURSE_CREATE_ERROR);
    }

    @Test
    void update_정상() {
        // given
        Course course = CourseFixtureBuilder.getCourse();
        String title = "바뀐 제목";
        String description = "바뀐 내용";
        int price = 20000;
        String targetAudience = "바뀐 대상자";

        // when
        course.update(title, description, price, targetAudience);

        // then
        assertThat(course.getTitle()).isEqualTo(title);
        assertThat(course.getDescription()).isEqualTo(description);
        assertThat(course.getPrice()).isEqualTo(price);
        assertThat(course.getTargetAudience()).isEqualTo(targetAudience);
    }

    @Test
    void deleteLogically_정상() {
        // given
        Course course = CourseFixtureBuilder.getCourse();

        // when
        course.deleteLogically();

        // then
        assertThat(course.isDeleted()).isTrue();
    }

    @Test
    void updateThumbnail_정상() {
        // given
        Course course = CourseFixtureBuilder.getCourse();
        File file = File.create(FileContainerType.COURSE_THUMBNAIL, 1L, "00000000-0000-0000-0000-000000000001.jpg",
                "thumb.jpg", "image/jpeg", 10);

        // when
        course.updateThumbnail(file);

        // then
        assertThat(course.getThumbnail()).isEqualTo(file);
    }

    @Test
    void deleteThumbnail_정상() {
        // given
        Course course = CourseFixtureBuilder.getCourse();
        File file = File.create(FileContainerType.COURSE_THUMBNAIL, 1L, "00000000-0000-0000-0000-000000000001.jpg",
                "thumb.jpg", "image/jpeg", 10);
        course.updateThumbnail(file);

        // when
        course.deleteThumbnail();

        // then
        assertThat(course.getThumbnail()).isNull();
    }
}