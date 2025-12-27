package insty.model.community.id;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import insty.error.CommunityErrorCode;
import insty.exception.CustomException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class CommunityCommentFileIdTest {

    @Test
    void create_정상() {
        // given
        Long commentId = 1L;
        Long fileId = 2L;

        // when
        CommunityCommentFileId id = CommunityCommentFileId.create(commentId, fileId);

        // then
        assertThat(id).isNotNull();
        assertThat(id.getCommentId()).isEqualTo(commentId);
        assertThat(id.getFileId()).isEqualTo(fileId);
    }

    @Test
    void equals_hashCode_정상() {
        // given
        Long commentId = 1L;
        Long fileId = 2L;

        CommunityCommentFileId id1 = CommunityCommentFileId.create(commentId, fileId);
        CommunityCommentFileId id2 = CommunityCommentFileId.create(commentId, fileId);

        // when, then
        assertThat(id1).isEqualTo(id2);
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
    }

    @Test
    void create_에러_commentId가_null이다() {
        // given
        Long commentId = null;
        Long fileId = 2L;

        // when, then
        assertThatThrownBy(() -> CommunityCommentFileId.create(commentId, fileId))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
    }

    @Test
    void create_에러_fileId가_null이다() {
        // given
        Long commentId = 1L;
        Long fileId = null;

        // when, then
        assertThatThrownBy(() -> CommunityCommentFileId.create(commentId, fileId))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
    }
}
