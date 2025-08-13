package insty.model.community;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import insty.error.CommunityErrorCode;
import insty.exception.CustomException;
import insty.model.course.Course;
import insty.model.course.CourseFixture;
import insty.model.course.CourseFixtureBuilder;
import insty.model.user.User;
import insty.model.user.UserFixture;
import insty.model.user.UserFixtureBuilder;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class CommunityQuestionTest {

    @Test
    void create_정상() {
        // given
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();
        User user = UserFixtureBuilder.getUserWithId();
        String title = "질문 제목";
        String content = "질문 내용";

        // when
        CommunityQuestion communityQuestion = CommunityQuestion.create(course, user, title, content);

        // then
        assertThat(communityQuestion).isNotNull();
        assertThat(communityQuestion.getId()).isNull();
        assertThat(communityQuestion.getCourse()).isEqualTo(course);
        assertThat(communityQuestion.getUser()).isEqualTo(user);
        assertThat(communityQuestion.getTitle()).isEqualTo(title);
        assertThat(communityQuestion.getContent()).isEqualTo(content);
        assertThat(communityQuestion.getStatus()).isEqualTo(QuestionStatus.WAITING);
        assertThat(communityQuestion.isDeleted()).isFalse();
    }

    @Test
    void create_에러_course가_null이다() {
        // given
        Course course = null;
        User user = UserFixtureBuilder.getUserWithId();
        String title = "질문 제목";
        String content = "질문 내용";

        // when

        // then
        assertThatThrownBy(() -> CommunityQuestion.create(course, user, title, content))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
    }

    @Test
    void create_에러_course_id가_null이다() {

        // given
        User user = UserFixtureBuilder.getUserWithId();
        Course course = CourseFixture.getCourse(user);
        String title = "질문 제목";
        String content = "질문 내용";

        // when

        // then
        assertThatThrownBy(() -> CommunityQuestion.create(course, user, title, content))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
    }

    @Test
    void create_에러_user가_null이다() {
        // given
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();
        User user = null;
        String title = "질문 제목";
        String content = "질문 내용";

        // when

        // then
        assertThatThrownBy(() -> CommunityQuestion.create(course, user, title, content))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
    }

    @Test
    void create_에러_user_id가_null이다() {
        // given
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();
        User user = UserFixture.getUser();
        String title = "질문 제목";
        String content = "질문 내용";

        // when

        // then
        assertThatThrownBy(() -> CommunityQuestion.create(course, user, title, content))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
    }

    @Test
    void create_에러_title이_null이다() {
        // given
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();
        User user = UserFixtureBuilder.getUserWithId();
        String title = null;
        String content = "질문 내용";

        // when

        // then
        assertThatThrownBy(() -> CommunityQuestion.create(course, user, title, content))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
    }

    @Test
    void create_에러_title에_공백만_있다() {
        // given
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();
        User user = UserFixtureBuilder.getUserWithId();
        String title = "   \n\t\r";
        String content = "질문 내용";

        // when

        // then
        assertThatThrownBy(() -> CommunityQuestion.create(course, user, title, content))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
    }

    @Test
    void create_에러_content가_null이다() {
        // given
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();
        User user = UserFixtureBuilder.getUserWithId();
        String title = "질문 제목";
        String content = null;

        // when

        // then
        assertThatThrownBy(() -> CommunityQuestion.create(course, user, title, content))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
    }

    @Test
    void create_에러_content에_공백만_있다() {
        // given
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();
        User user = UserFixtureBuilder.getUserWithId();
        String title = "질문 제목";
        String content = "   \n\t\r";

        // when

        // then
        assertThatThrownBy(() -> CommunityQuestion.create(course, user, title, content))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
    }

    @Test
    void update_정상() {
        // given
        CommunityQuestion communityQuestion = CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser();
        String title = "수정된 제목";
        String content = "수정된 내용";
        List<CommunityQuestionFile> attachments = new ArrayList<>();

        // when
        communityQuestion.update(title, content, attachments);

        // then
        assertThat(communityQuestion.getTitle()).isEqualTo(title);
        assertThat(communityQuestion.getContent()).isEqualTo(content);
        assertThat(communityQuestion.getAttachments()).isEqualTo(attachments);
    }

    @Test
    void acceptAnswer_정상() {
        // given
        CommunityQuestion communityQuestion = CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser();
        CommunityAnswer answer = CommunityAnswerFixtureBuilder.getCommunityAnswerWithIdAndUser(communityQuestion);

        // when
        communityQuestion.acceptAnswer(answer);

        // then
        assertThat(communityQuestion.getAcceptedAnswer()).isEqualTo(answer);
        assertThat(communityQuestion.getStatus()).isEqualTo(QuestionStatus.ACCEPTED);
        assertThat(answer.isAccepted()).isTrue();
    }

    @Test
    void acceptAnswer_기존_답변_채택_해제() {
        // given
        CommunityQuestion communityQuestion = CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser();
        CommunityAnswer firstAnswer = CommunityAnswerFixtureBuilder.getCommunityAnswerWithIdAndUser(communityQuestion);
        CommunityAnswer secondAnswer = CommunityAnswerFixtureBuilder.getCommunityAnswerWithIdAndUser(communityQuestion, 2L, "두 번째 답변");

        communityQuestion.acceptAnswer(firstAnswer);

        // when
        communityQuestion.acceptAnswer(secondAnswer);

        // then
        assertThat(communityQuestion.getAcceptedAnswer()).isEqualTo(secondAnswer);
        assertThat(communityQuestion.getStatus()).isEqualTo(QuestionStatus.ACCEPTED);
        assertThat(firstAnswer.isAccepted()).isFalse();
        assertThat(secondAnswer.isAccepted()).isTrue();
    }

    @Test
    void unacceptAnswer_정상() {
        // given
        CommunityQuestion communityQuestion = CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser();
        CommunityAnswer answer = CommunityAnswerFixtureBuilder.getCommunityAnswerWithIdAndUser(communityQuestion);
        communityQuestion.acceptAnswer(answer);

        // when
        communityQuestion.unacceptAnswer();

        // then
        assertThat(communityQuestion.getAcceptedAnswer()).isNull();
        assertThat(communityQuestion.getStatus()).isEqualTo(QuestionStatus.ANSWERED);
        assertThat(answer.isAccepted()).isFalse();
    }

    @Test
    void changeStatusByAnswer_답변_있음() {
        // given
        CommunityQuestion communityQuestion = CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser();
        
        // when
        communityQuestion.changeStatusByAnswer(true);
        
        // then
        assertThat(communityQuestion.getStatus()).isEqualTo(QuestionStatus.ANSWERED);
    }

    @Test
    void changeStatusByAnswer_답변_없음() {
        // given
        CommunityQuestion communityQuestion = CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser();
        communityQuestion.changeStatusByAnswer(true); // 먼저 ANSWERED 상태로 변경
        
        // when
        communityQuestion.changeStatusByAnswer(false);
        
        // then
        assertThat(communityQuestion.getStatus()).isEqualTo(QuestionStatus.WAITING);
    }

    @Test
    void unacceptAnswer_채택된_답변이_없는_경우() {
        // given
        CommunityQuestion communityQuestion = CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser();
        
        // when
        communityQuestion.unacceptAnswer();
        
        // then
        assertThat(communityQuestion.getAcceptedAnswer()).isNull();
        assertThat(communityQuestion.getStatus()).isEqualTo(QuestionStatus.ANSWERED);
    }

    @Test
    void handleAcceptedAnswerDeleted_남은답변있음() {
        // given
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();
        User user = UserFixtureBuilder.getUserWithId();
        CommunityQuestion communityQuestion = CommunityQuestion.create(course, user, "질문", "내용");
        
        CommunityAnswer acceptedAnswer = CommunityAnswer.create(communityQuestion, user, "채택된 답변");
        acceptedAnswer.accept();
        communityQuestion.acceptAnswer(acceptedAnswer);

        // when
        communityQuestion.handleAcceptedAnswerDeleted(true); // 남은 답변 있음

        // then
        assertThat(communityQuestion.getAcceptedAnswer()).isNull();
        assertThat(communityQuestion.getStatus()).isEqualTo(QuestionStatus.ANSWERED);
        assertThat(acceptedAnswer.isAccepted()).isFalse();
    }

    @Test
    void handleAcceptedAnswerDeleted_남은답변없음() {
        // given
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();
        User user = UserFixtureBuilder.getUserWithId();
        CommunityQuestion communityQuestion = CommunityQuestion.create(course, user, "질문", "내용");
        
        CommunityAnswer acceptedAnswer = CommunityAnswer.create(communityQuestion, user, "채택된 답변");
        acceptedAnswer.accept();
        communityQuestion.acceptAnswer(acceptedAnswer);

        // when
        communityQuestion.handleAcceptedAnswerDeleted(false); // 남은 답변 없음

        // then
        assertThat(communityQuestion.getAcceptedAnswer()).isNull();
        assertThat(communityQuestion.getStatus()).isEqualTo(QuestionStatus.WAITING);
        assertThat(acceptedAnswer.isAccepted()).isFalse();
    }

    @Test
    void handleAcceptedAnswerDeleted_채택된답변없는경우() {
        // given
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();
        User user = UserFixtureBuilder.getUserWithId();
        CommunityQuestion communityQuestion = CommunityQuestion.create(course, user, "질문", "내용");

        // when
        communityQuestion.handleAcceptedAnswerDeleted(true); // 남은 답변 있음

        // then
        assertThat(communityQuestion.getAcceptedAnswer()).isNull();
        assertThat(communityQuestion.getStatus()).isEqualTo(QuestionStatus.ANSWERED);
    }
}
