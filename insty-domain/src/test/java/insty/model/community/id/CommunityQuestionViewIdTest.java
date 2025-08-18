package insty.model.community.id;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import insty.error.CommunityErrorCode;
import insty.exception.CustomException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
public class CommunityQuestionViewIdTest {

    @Test
    void create_정상() {
        // given
        Long questionId = 1L;
        Long userId = 2L;

        // when
        CommunityQuestionViewId communityQuestionViewId = CommunityQuestionViewId.create(questionId, userId);

        // then
        assertThat(communityQuestionViewId).isNotNull();
        assertThat(communityQuestionViewId.getCommunityQuestion()).isEqualTo(questionId);
        assertThat(communityQuestionViewId.getUserId()).isEqualTo(userId);
    }

    @Test
    void equals_hashCode_정상() {
        // given
        Long questionId = 1L;
        Long userId = 2L;

        CommunityQuestionViewId communityQuestionViewId1 = CommunityQuestionViewId.create(questionId, userId);
        CommunityQuestionViewId communityQuestionViewId2 = CommunityQuestionViewId.create(questionId, userId);

        // when, then
        assertThat(communityQuestionViewId1).isEqualTo(communityQuestionViewId2);
        assertThat(communityQuestionViewId1.hashCode()).isEqualTo(communityQuestionViewId2.hashCode());
    }

    @Test
    void equals_다른객체_false() {
        // given
        Long questionId1 = 1L;
        Long userId1 = 2L;
        Long questionId2 = 3L;
        Long userId2 = 4L;

        CommunityQuestionViewId communityQuestionViewId1 = CommunityQuestionViewId.create(questionId1, userId1);
        CommunityQuestionViewId communityQuestionViewId2 = CommunityQuestionViewId.create(questionId2, userId2);

        // when, then
        assertThat(communityQuestionViewId1).isNotEqualTo(communityQuestionViewId2);
        assertThat(communityQuestionViewId1.hashCode()).isNotEqualTo(communityQuestionViewId2.hashCode());
    }

    @Test
    void create_에러_questionId가Null_예외() {
        // given
        Long questionId = null;
        Long userId = 2L;

        // when, then
        assertThatThrownBy(() -> CommunityQuestionViewId.create(questionId, userId))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
    }

    @Test
    void create_에러_userId가Null_예외() {
        // given
        Long questionId = 1L;
        Long userId = null;

        // when, then
        assertThatThrownBy(() -> CommunityQuestionViewId.create(questionId, userId))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
    }
}
