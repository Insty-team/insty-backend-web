package insty.model.courseqna;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import insty.error.CourseQnaErrorCode;
import insty.exception.CustomException;
import insty.model.user.User;
import insty.model.user.UserFixtureBuilder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class CourseAnswerTest {

    @Test
    void create_정상() {
        // given
        CourseQuestion courseQuestion = CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser();
        User user = UserFixtureBuilder.getUserWithId();
        String content = "답변 내용";

        // when
        CourseAnswer courseAnswer = CourseAnswer.create(courseQuestion, user, content);

        // then
        assertThat(courseAnswer).isNotNull();
        assertThat(courseAnswer.getId()).isNull();
        assertThat(courseAnswer.getCourseQuestion()).isEqualTo(courseQuestion);
        assertThat(courseAnswer.getUser()).isEqualTo(user);
        assertThat(courseAnswer.getContent()).isEqualTo(content);
        assertThat(courseAnswer.isDeleted()).isFalse();
        assertThat(courseAnswer.isAccepted()).isFalse();
    }

    @Test
    void create_에러_courseQuestion이_null이다() {
        // given
        CourseQuestion courseQuestion = null;
        User user = UserFixtureBuilder.getUserWithId();
        String content = "답변 내용";

        // when

        // then
        assertThatThrownBy(() -> CourseAnswer.create(courseQuestion, user, content))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CourseQnaErrorCode.COURSE_QNA_CREATE_ERROR);
    }

    @Test
    void create_에러_user가_null이다() {
        // given
        CourseQuestion courseQuestion = CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser();
        User user = null;
        String content = "답변 내용";

        // when

        // then
        assertThatThrownBy(() -> CourseAnswer.create(courseQuestion, user, content))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CourseQnaErrorCode.COURSE_QNA_CREATE_ERROR);
    }

    @Test
    void create_에러_content가_null이다() {
        // given
        CourseQuestion courseQuestion = CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser();
        User user = UserFixtureBuilder.getUserWithId();
        String content = null;

        // when

        // then
        assertThatThrownBy(() -> CourseAnswer.create(courseQuestion, user, content))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CourseQnaErrorCode.COURSE_QNA_CREATE_ERROR);
    }

    @Test
    void create_에러_content에_공백만_있다() {
        // given
        CourseQuestion courseQuestion = CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser();
        User user = UserFixtureBuilder.getUserWithId();
        String content = "   \n\t\r";

        // when

        // then
        assertThatThrownBy(() -> CourseAnswer.create(courseQuestion, user, content))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CourseQnaErrorCode.COURSE_QNA_CREATE_ERROR);
    }

    @Test
    void update_정상() {
        // given
        CourseAnswer courseAnswer = CommunityAnswerFixtureBuilder.getCommunityAnswerWithIdAndUser(
                CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser());
        String content = "수정된 답변 내용";

        // when
        courseAnswer.update(content);

        // then
        assertThat(courseAnswer.getContent()).isEqualTo(content);
    }

    @Test
    void accept_정상() {
        // given
        CourseAnswer courseAnswer = CommunityAnswerFixtureBuilder.getCommunityAnswerWithIdAndUser(
                CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser());

        // when
        courseAnswer.accept();

        // then
        assertThat(courseAnswer.isAccepted()).isTrue();
    }

    @Test
    void unaccept_정상() {
        // given
        CourseAnswer courseAnswer = CommunityAnswerFixtureBuilder.getCommunityAnswerWithIdAndUser(
                CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser());
        courseAnswer.accept();

        // when
        courseAnswer.unaccept();

        // then
        assertThat(courseAnswer.isAccepted()).isFalse();
    }
}
