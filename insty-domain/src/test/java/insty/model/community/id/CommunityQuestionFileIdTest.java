package insty.model.community.id;

import insty.exception.CustomException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import insty.error.CommunityErrorCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("unit")
public class CommunityQuestionFileIdTest {

    @Test
    void create_정상() {
        // given
        Long fileId = 1L;
        Long questionId = 2L;

        // when
        CommunityQuestionFileId communityQuestionFileId = CommunityQuestionFileId.create(questionId, fileId);

        // then
        assertThat(communityQuestionFileId).isNotNull();
        assertThat(communityQuestionFileId.getFileId()).isEqualTo(fileId);
        assertThat(communityQuestionFileId.getQuestionId()).isEqualTo(questionId);
    }

    @Test
    void equals_hashCode_정상() {
        // given
        Long fileId = 1L;
        Long questionId = 2L;

        CommunityQuestionFileId communityQuestionFileId1 = CommunityQuestionFileId.create(questionId, fileId);
        CommunityQuestionFileId communityQuestionFileId2 = CommunityQuestionFileId.create(questionId, fileId);

        // when, then
        assertThat(communityQuestionFileId1).isEqualTo(communityQuestionFileId2);
        assertThat(communityQuestionFileId1.hashCode()).isEqualTo(communityQuestionFileId2.hashCode());
    }

    @Test
    void create_에러_questionId가Null_예외() {
        // given
        Long fileId = 1L;
        Long questionId = null;

        // when, then
        assertThatThrownBy(() -> CommunityQuestionFileId.create(questionId, fileId))
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
        assertThatThrownBy(() -> CommunityQuestionFileId.create(fileId, questionId))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
    }
}
