package insty.model.community.id;

import insty.exception.CustomException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import insty.error.CommunityErrorCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("unit")
public class CommunityFileIdTest {

    @Test
    void create_정상() {
        // given
        Long fileId = 1L;
        Long questionId = 2L;

        // when
        CommunityFileId communityFileId = CommunityFileId.create(questionId, fileId);

        // then
        assertThat(communityFileId).isNotNull();
        assertThat(communityFileId.getFileId()).isEqualTo(fileId);
        assertThat(communityFileId.getQuestionId()).isEqualTo(questionId);
    }

    @Test
    void equals_hashCode_정상() {
        // given
        Long fileId = 1L;
        Long questionId = 2L;

        CommunityFileId communityFileId1 = CommunityFileId.create(questionId, fileId);
        CommunityFileId communityFileId2 = CommunityFileId.create(questionId, fileId);

        // when, then
        assertThat(communityFileId1).isEqualTo(communityFileId2);
        assertThat(communityFileId1.hashCode()).isEqualTo(communityFileId2.hashCode());
    }

    @Test
    void create_에러_questionId가Null_예외() {
        // given
        Long fileId = 1L;
        Long questionId = null;

        // when, then
        assertThatThrownBy(() -> CommunityFileId.create(questionId, fileId))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
    }

    @Test
    void create_파일ID가Null_예외() {
        // given
        Long fileId = null;
        Long questionId = 2L;

        // when, then
        assertThatThrownBy(() -> CommunityFileId.create(fileId, questionId))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
    }
}
