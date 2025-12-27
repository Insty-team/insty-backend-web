package insty.model.community;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import insty.error.CommunityErrorCode;
import insty.exception.CustomException;
import insty.model.file.File;
import insty.model.file.FileContainerType;
import insty.model.file.FileFixtureBuilder;
import insty.model.user.User;
import insty.model.user.UserFixtureBuilder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class CommunityCommentTest {

    @Test
    void create_정상() {
        // given
        CommunityPost post = CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser();
        User user = UserFixtureBuilder.getUserWithId();
        String content = "댓글 내용";

        // when
        CommunityComment comment = CommunityComment.create(post, user, content);

        // then
        assertThat(comment).isNotNull();
        assertThat(comment.getId()).isNull();
        assertThat(comment.getCommunityPost()).isEqualTo(post);
        assertThat(comment.getUser()).isEqualTo(user);
        assertThat(comment.getContent()).isEqualTo(content);
        assertThat(comment.isDeleted()).isFalse();
    }

    @Test
    void create_에러_post가_null이다() {
        // given
        CommunityPost post = null;
        User user = UserFixtureBuilder.getUserWithId();
        String content = "댓글 내용";

        // when, then
        assertThatThrownBy(() -> CommunityComment.create(post, user, content))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
    }

    @Test
    void create_에러_user가_null이다() {
        // given
        CommunityPost post = CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser();
        User user = null;
        String content = "댓글 내용";

        // when, then
        assertThatThrownBy(() -> CommunityComment.create(post, user, content))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
    }

    @Test
    void create_에러_content가_null이다() {
        // given
        CommunityPost post = CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser();
        User user = UserFixtureBuilder.getUserWithId();
        String content = null;

        // when, then
        assertThatThrownBy(() -> CommunityComment.create(post, user, content))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
    }

    @Test
    void create_에러_content가_공백이다() {
        // given
        CommunityPost post = CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser();
        User user = UserFixtureBuilder.getUserWithId();
        String content = "   \n\t\r";

        // when, then
        assertThatThrownBy(() -> CommunityComment.create(post, user, content))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
    }

    @Test
    void update_정상() {
        // given
        CommunityPost post = CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser();
        CommunityComment comment = CommunityCommentFixtureBuilder.getCommunityCommentWithIdAndUser(post);
        String updatedContent = "수정된 댓글 내용";

        // when
        comment.update(updatedContent);

        // then
        assertThat(comment.getContent()).isEqualTo(updatedContent);
    }

    @Test
    void removeAllFiles_정상() {
        // given
        CommunityPost post = CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser();
        CommunityComment comment = CommunityCommentFixtureBuilder.getCommunityCommentWithIdAndUser(post);
        File file = FileFixtureBuilder.getFileWithId(1L, FileContainerType.COMMUNITY_COMMENT_IMAGE, comment.getId(),
                "file.png", "file.png", "image/png", 100);
        CommunityCommentFile attachment = CommunityCommentFile.create(comment, file);
        comment.getAttachments().add(attachment);
        assertThat(comment.getAttachments()).isNotEmpty();

        // when
        comment.removeAllFiles();

        // then
        assertThat(comment.getAttachments()).isEmpty();
    }

    @Test
    void markAsDeleted_정상() {
        // given
        CommunityPost post = CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser();
        CommunityComment comment = CommunityCommentFixtureBuilder.getCommunityCommentWithIdAndUser(post);

        // when
        comment.markAsDeleted();

        // then
        assertThat(comment.isDeleted()).isTrue();
    }
}
