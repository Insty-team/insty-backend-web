package insty.model.community;

import insty.exception.CustomException;
import insty.model.course.Course;
import insty.model.course.CourseFixtureBuilder;
import insty.model.user.User;
import insty.model.user.UserFixtureBuilder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import insty.error.CommunityErrorCode;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("unit")
class CommunityQuestionTest {

    @Test
    void create_정상() {
        // given
        String title = "제목";
        String content = "내용";
        Long userId = 1L;
        Long courseId = 2L;

        User user = UserFixtureBuilder.getUserWithId();
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();

        CommunityQuestion communityQuestion = CommunityQuestion.create(
                course,
                user,
                title,
                content
        );

        //then
        assertThat(communityQuestion).isNotNull();
        assertThat(communityQuestion.getTitle()).isEqualTo(title);
        assertThat(communityQuestion.getContent()).isEqualTo(content);
    }

    @Test
    void create_에러_제목이_null() {
        // given
        String title = null;
        String content = "내용";
        Long userId = 1L;
        Long courseId = 2L;

        User user = UserFixtureBuilder.getUserWithId();
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();

        // when & then
        assertThatThrownBy(() -> CommunityQuestion.create(
                course,
                user,
                title,
                content
        )).isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
    }

    @Test
    void create_에러_내용이_null() {
        // given
        String title = "제목";
        String content = null;
        Long userId = 1L;
        Long courseId = 2L;

        User user = UserFixtureBuilder.getUserWithId();
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();

        // when & then
        assertThatThrownBy(() -> CommunityQuestion.create(
                course,
                user,
                title,
                content
        )).isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
    }

    @Test
    void create_에러_유저가_null() {
        // given
        String title = "제목";
        String content = "내용";
        User user = null;
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();

        // when & then
        assertThatThrownBy(() -> CommunityQuestion.create(
                course,
                user,
                title,
                content
        )).isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
    }

    @Test
    void create_에러_강의가_null() {
        // given
        String title = "제목";
        String content = "내용";
        User user = UserFixtureBuilder.getUserWithId();
        Course course = null;

        // when & then
        assertThatThrownBy(() -> CommunityQuestion.create(
                course,
                user,
                title,
                content
        )).isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
    }
}
