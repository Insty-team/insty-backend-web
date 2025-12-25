package insty.model.mention;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import insty.error.MentionErrorCode;
import insty.exception.CustomException;
import insty.model.courseqna.CommunityAnswerFixtureBuilder;
import insty.model.courseqna.CommunityQuestionFixtureBuilder;
import insty.model.courseqna.CourseAnswer;
import insty.model.user.User;
import insty.model.user.UserFixtureBuilder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class MentionTest {

    @Test
    void create_정상() {
        // given
        CourseAnswer courseAnswer = CommunityAnswerFixtureBuilder.getCommunityAnswerWithIdAndUser(
                CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser()
        );
        User mentionedUser = UserFixtureBuilder.getUserWithId(1L);
        User mentionerUser = UserFixtureBuilder.getUserWithId(2L);

        // when
        Mention mention = Mention.create(courseAnswer, mentionedUser, mentionerUser);

        // then
        assertThat(mention).isNotNull();
        assertThat(mention.getId()).isNull();
        assertThat(mention.getCourseAnswer()).isEqualTo(courseAnswer);
        assertThat(mention.getMentionedUser()).isEqualTo(mentionedUser);
        assertThat(mention.getMentionerUser()).isEqualTo(mentionerUser);
    }

    @Test
    void create_에러_CommunityAnswer가_null이다() {
        // given
        CourseAnswer courseAnswer = null;
        User mentionedUser = UserFixtureBuilder.getUserWithId(1L);
        User mentionerUser = UserFixtureBuilder.getUserWithId(2L);

        // when & then
        assertThatThrownBy(() -> Mention.create(courseAnswer, mentionedUser, mentionerUser))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(MentionErrorCode.MENTION_CREATE_ERROR);
    }

    @Test
    void create_에러_MentionedUser가_null이다() {
        // given
        CourseAnswer courseAnswer = CommunityAnswerFixtureBuilder.getCommunityAnswerWithIdAndUser(
                CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser()
        );
        User mentionedUser = null;
        User mentionerUser = UserFixtureBuilder.getUserWithId(2L);

        // when & then
        assertThatThrownBy(() -> Mention.create(courseAnswer, mentionedUser, mentionerUser))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(MentionErrorCode.MENTION_CREATE_ERROR);
    }

    @Test
    void create_에러_MentionerUser가_null이다() {
        // given
        CourseAnswer courseAnswer = CommunityAnswerFixtureBuilder.getCommunityAnswerWithIdAndUser(
                CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser()
        );
        User mentionedUser = UserFixtureBuilder.getUserWithId(1L);
        User mentionerUser = null;

        // when & then
        assertThatThrownBy(() -> Mention.create(courseAnswer, mentionedUser, mentionerUser))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(MentionErrorCode.MENTION_CREATE_ERROR);
    }

    @Test
    void create_에러_자기_자신을_멘션한다() {
        // given
        CourseAnswer courseAnswer = CommunityAnswerFixtureBuilder.getCommunityAnswerWithIdAndUser(
                CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser()
        );
        User user = UserFixtureBuilder.getUserWithId(1L);

        // when & then
        assertThatThrownBy(() -> Mention.create(courseAnswer, user, user))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(MentionErrorCode.MENTION_SELF_ERROR);
    }
}
