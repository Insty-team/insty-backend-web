package insty.model.community;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import insty.error.CommunityErrorCode;
import insty.exception.CustomException;
import insty.model.user.User;
import insty.model.user.UserFixtureBuilder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class CommunityAnswerTest {

    @Test
    void create_정상() {
        // given
        CommunityQuestion communityQuestion = CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser();
        User user = UserFixtureBuilder.getUserWithId();
        String content = "답변 내용";

        // when
        CommunityAnswer communityAnswer = CommunityAnswer.create(communityQuestion, user, content);

        // then
        assertThat(communityAnswer).isNotNull();
        assertThat(communityAnswer.getId()).isNull();
        assertThat(communityAnswer.getCommunityQuestion()).isEqualTo(communityQuestion);
        assertThat(communityAnswer.getUser()).isEqualTo(user);
        assertThat(communityAnswer.getContent()).isEqualTo(content);
        assertThat(communityAnswer.isDeleted()).isFalse();
        assertThat(communityAnswer.isAccepted()).isFalse();
    }

    @Test
    void create_에러_communityQuestion이_null이다() {
        // given
        CommunityQuestion communityQuestion = null;
        User user = UserFixtureBuilder.getUserWithId();
        String content = "답변 내용";

        // when

        // then
        assertThatThrownBy(() -> CommunityAnswer.create(communityQuestion, user, content))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
    }

    @Test
    void create_에러_user가_null이다() {
        // given
        CommunityQuestion communityQuestion = CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser();
        User user = null;
        String content = "답변 내용";

        // when

        // then
        assertThatThrownBy(() -> CommunityAnswer.create(communityQuestion, user, content))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
    }

    @Test
    void create_에러_content가_null이다() {
        // given
        CommunityQuestion communityQuestion = CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser();
        User user = UserFixtureBuilder.getUserWithId();
        String content = null;

        // when

        // then
        assertThatThrownBy(() -> CommunityAnswer.create(communityQuestion, user, content))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
    }

    @Test
    void create_에러_content에_공백만_있다() {
        // given
        CommunityQuestion communityQuestion = CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser();
        User user = UserFixtureBuilder.getUserWithId();
        String content = "   \n\t\r";

        // when

        // then
        assertThatThrownBy(() -> CommunityAnswer.create(communityQuestion, user, content))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
    }

    @Test
    void update_정상() {
        // given
        CommunityAnswer communityAnswer = CommunityAnswerFixtureBuilder.getCommunityAnswerWithIdAndUser(
                CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser());
        String content = "수정된 답변 내용";

        // when
        communityAnswer.update(content);

        // then
        assertThat(communityAnswer.getContent()).isEqualTo(content);
    }

    @Test
    void accept_정상() {
        // given
        CommunityAnswer communityAnswer = CommunityAnswerFixtureBuilder.getCommunityAnswerWithIdAndUser(
                CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser());

        // when
        communityAnswer.accept();

        // then
        assertThat(communityAnswer.isAccepted()).isTrue();
    }

    @Test
    void unaccept_정상() {
        // given
        CommunityAnswer communityAnswer = CommunityAnswerFixtureBuilder.getCommunityAnswerWithIdAndUser(
                CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser());
        communityAnswer.accept();

        // when
        communityAnswer.unaccept();

        // then
        assertThat(communityAnswer.isAccepted()).isFalse();
    }
}
