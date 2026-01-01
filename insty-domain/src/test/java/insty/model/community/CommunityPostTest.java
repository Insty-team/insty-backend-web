package insty.model.community;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import insty.error.CommunityErrorCode;
import insty.exception.CustomException;
import insty.model.course.Course;
import insty.model.course.CourseFixtureBuilder;
import insty.model.file.File;
import insty.model.file.FileContainerType;
import insty.model.file.FileFixtureBuilder;
import insty.model.user.User;
import insty.model.user.UserFixture;
import insty.model.user.UserFixtureBuilder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class CommunityPostTest {

    @Test
    void create_정상() {
        // given
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();
        User user = UserFixtureBuilder.getUserWithId(2L);
        String title = "게시글 제목";
        String content = "게시글 내용";

        // when
        CommunityPost post = CommunityPost.create(user, course, title, content);

        // then
        assertThat(post).isNotNull();
        assertThat(post.getId()).isNull();
        assertThat(post.getUser()).isEqualTo(user);
        assertThat(post.getTitle()).isEqualTo(title);
        assertThat(post.getContent()).isEqualTo(content);
        assertThat(post.isDeleted()).isFalse();
    }

    @Test
    void create_에러_user가_null이다() {
        // given
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();
        User user = null;
        String title = "게시글 제목";
        String content = "게시글 내용";

        // when, then
        assertThatThrownBy(() -> CommunityPost.create(user, course, title, content))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
    }

    @Test
    void create_에러_user_id가_null이다() {
        // given
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();
        User user = UserFixture.getUser();
        String title = "게시글 제목";
        String content = "게시글 내용";

        // when, then
        assertThatThrownBy(() -> CommunityPost.create(user, course, title, content))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
    }

    @Test
    void create_에러_course가_null이다() {
        // given
        User user = UserFixtureBuilder.getUserWithId();
        Course course = null;
        String title = "게시글 제목";
        String content = "게시글 내용";

        // when, then
        assertThatThrownBy(() -> CommunityPost.create(user, course, title, content))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
    }

    @Test
    void create_에러_title이_null이다() {
        // given
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();
        User user = UserFixtureBuilder.getUserWithId(2L);
        String title = null;
        String content = "게시글 내용";

        // when, then
        assertThatThrownBy(() -> CommunityPost.create(user, course, title, content))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
    }

    @Test
    void create_에러_title이_공백이다() {
        // given
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();
        User user = UserFixtureBuilder.getUserWithId(2L);
        String title = "   \n\t\r";
        String content = "게시글 내용";

        // when, then
        assertThatThrownBy(() -> CommunityPost.create(user, course, title, content))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
    }

    @Test
    void create_에러_content가_null이다() {
        // given
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();
        User user = UserFixtureBuilder.getUserWithId(2L);
        String title = "게시글 제목";
        String content = null;

        // when, then
        assertThatThrownBy(() -> CommunityPost.create(user, course, title, content))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
    }

    @Test
    void create_에러_content가_공백이다() {
        // given
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();
        User user = UserFixtureBuilder.getUserWithId(2L);
        String title = "게시글 제목";
        String content = "   \n\t\r";

        // when, then
        assertThatThrownBy(() -> CommunityPost.create(user, course, title, content))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
    }

    @Test
    void update_정상() {
        // given
        CommunityPost post = CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser();
        String title = "수정된 제목";
        String content = "수정된 내용";

        // when
        post.update(title, content);

        // then
        assertThat(post.getTitle()).isEqualTo(title);
        assertThat(post.getContent()).isEqualTo(content);
    }

    @Test
    void removeAllFiles_정상() {
        // given
        CommunityPost post = CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser();
        File file = FileFixtureBuilder.getFileWithId(1L, FileContainerType.COMMUNITY_POST_IMAGE, post.getId(),
                "file.png", "file.png", "image/png", 100);
        CommunityPostFile attachment = CommunityPostFile.create(post, file);
        post.getAttachments().add(attachment);
        assertThat(post.getAttachments()).isNotEmpty();

        // when
        post.removeAllFiles();

        // then
        assertThat(post.getAttachments()).isEmpty();
    }

    @Test
    void markAsDeleted_정상() {
        // given
        CommunityPost post = CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser();

        // when
        post.markAsDeleted();

        // then
        assertThat(post.isDeleted()).isTrue();
    }
}
