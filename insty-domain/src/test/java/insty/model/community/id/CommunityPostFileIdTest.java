package insty.model.community.id;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import insty.error.CommunityErrorCode;
import insty.exception.CustomException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class CommunityPostFileIdTest {

    @Test
    void create_정상() {
        // given
        Long postId = 1L;
        Long fileId = 2L;

        // when
        CommunityPostFileId id = CommunityPostFileId.create(postId, fileId);

        // then
        assertThat(id).isNotNull();
        assertThat(id.getPostId()).isEqualTo(postId);
        assertThat(id.getFileId()).isEqualTo(fileId);
    }

    @Test
    void equals_hashCode_정상() {
        // given
        Long postId = 1L;
        Long fileId = 2L;

        CommunityPostFileId id1 = CommunityPostFileId.create(postId, fileId);
        CommunityPostFileId id2 = CommunityPostFileId.create(postId, fileId);

        // when, then
        assertThat(id1).isEqualTo(id2);
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
    }

    @Test
    void create_에러_postId가_null이다() {
        // given
        Long postId = null;
        Long fileId = 2L;

        // when, then
        assertThatThrownBy(() -> CommunityPostFileId.create(postId, fileId))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
    }

    @Test
    void create_에러_fileId가_null이다() {
        // given
        Long postId = 1L;
        Long fileId = null;

        // when, then
        assertThatThrownBy(() -> CommunityPostFileId.create(postId, fileId))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
    }
}
