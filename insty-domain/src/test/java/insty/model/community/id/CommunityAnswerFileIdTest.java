package insty.model.community.id;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import insty.error.CommunityErrorCode;
import insty.exception.CustomException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
public class CommunityAnswerFileIdTest {

    @Test
    void create_정상() {
        // given
        Long answerId = 1L;
        Long fileId = 2L;

        // when
        CommunityAnswerFileId communityAnswerFileId = CommunityAnswerFileId.create(answerId, fileId);

        // then
        assertThat(communityAnswerFileId).isNotNull();
        assertThat(communityAnswerFileId.getAnswerId()).isEqualTo(answerId);
        assertThat(communityAnswerFileId.getFileId()).isEqualTo(fileId);
    }

    @Test
    void equals_hashCode_정상() {
        // given
        Long answerId = 1L;
        Long fileId = 2L;

        CommunityAnswerFileId communityAnswerFileId1 = CommunityAnswerFileId.create(answerId, fileId);
        CommunityAnswerFileId communityAnswerFileId2 = CommunityAnswerFileId.create(answerId, fileId);

        // when, then
        assertThat(communityAnswerFileId1).isEqualTo(communityAnswerFileId2);
        assertThat(communityAnswerFileId1.hashCode()).isEqualTo(communityAnswerFileId2.hashCode());
    }

    @Test
    void equals_다른객체_false() {
        // given
        Long answerId1 = 1L;
        Long fileId1 = 2L;
        Long answerId2 = 3L;
        Long fileId2 = 4L;

        CommunityAnswerFileId communityAnswerFileId1 = CommunityAnswerFileId.create(answerId1, fileId1);
        CommunityAnswerFileId communityAnswerFileId2 = CommunityAnswerFileId.create(answerId2, fileId2);

        // when, then
        assertThat(communityAnswerFileId1).isNotEqualTo(communityAnswerFileId2);
        assertThat(communityAnswerFileId1.hashCode()).isNotEqualTo(communityAnswerFileId2.hashCode());
    }

    @Test
    void create_에러_answerId가Null_예외() {
        // given
        Long answerId = null;
        Long fileId = 2L;

        // when, then
        assertThatThrownBy(() -> CommunityAnswerFileId.create(answerId, fileId))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
    }

    @Test
    void create_에러_fileId가Null_예외() {
        // given
        Long answerId = 1L;
        Long fileId = null;

        // when, then
        assertThatThrownBy(() -> CommunityAnswerFileId.create(answerId, fileId))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
    }
}
