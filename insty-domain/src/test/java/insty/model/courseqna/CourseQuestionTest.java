package insty.model.courseqna;

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
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class CourseQuestionTest {

    @Test
    void create_정상() {
        // given
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();
        User user = UserFixtureBuilder.getUserWithId();
        String title = "질문 제목";
        String content = "질문 내용";

        // when
        CourseQuestion courseQuestion = CourseQuestion.create(course, user, title, content, null);

        // then
        assertThat(courseQuestion).isNotNull();
        assertThat(courseQuestion.getId()).isNull();
        assertThat(courseQuestion.getCourse()).isEqualTo(course);
        assertThat(courseQuestion.getUser()).isEqualTo(user);
        assertThat(courseQuestion.getTitle()).isEqualTo(title);
        assertThat(courseQuestion.getContent()).isEqualTo(content);
        assertThat(courseQuestion.getStatus()).isEqualTo(QuestionStatus.WAITING);
        assertThat(courseQuestion.isDeleted()).isFalse();
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
        assertThatThrownBy(() -> CourseQuestion.create(course, user, title, content, null))
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
        assertThatThrownBy(() -> CourseQuestion.create(course, user, title, content, null))
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
        assertThatThrownBy(() -> CourseQuestion.create(course, user, title, content, null))
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
        assertThatThrownBy(() -> CourseQuestion.create(course, user, title, content, null))
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
        assertThatThrownBy(() -> CourseQuestion.create(course, user, title, content, null))
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
        assertThatThrownBy(() -> CourseQuestion.create(course, user, title, content, null))
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
        assertThatThrownBy(() -> CourseQuestion.create(course, user, title, content, null))
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
        assertThatThrownBy(() -> CourseQuestion.create(course, user, title, content, null))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
    }

    @Test
    void update_정상() {
        // given
        CourseQuestion courseQuestion = CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser();
        String title = "수정된 제목";
        String content = "수정된 내용";

        // when
        courseQuestion.update(title, content);

        // then
        assertThat(courseQuestion.getTitle()).isEqualTo(title);
        assertThat(courseQuestion.getContent()).isEqualTo(content);
    }

    @Test
    void acceptAnswer_정상() {
        // given
        CourseQuestion courseQuestion = CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser();
        CourseAnswer answer = CommunityAnswerFixtureBuilder.getCommunityAnswerWithIdAndUser(courseQuestion);

        // when
        courseQuestion.acceptAnswer(answer);

        // then
        assertThat(courseQuestion.getAcceptedAnswer()).isEqualTo(answer);
        assertThat(courseQuestion.getStatus()).isEqualTo(QuestionStatus.ACCEPTED);
        assertThat(answer.isAccepted()).isTrue();
    }

    @Test
    void acceptAnswer_기존_답변_채택_해제() {
        // given
        CourseQuestion courseQuestion = CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser();
        CourseAnswer firstAnswer = CommunityAnswerFixtureBuilder.getCommunityAnswerWithIdAndUser(courseQuestion);
        CourseAnswer secondAnswer = CommunityAnswerFixtureBuilder.getCommunityAnswerWithIdAndUser(courseQuestion, 2L, "두 번째 답변");

        courseQuestion.acceptAnswer(firstAnswer);

        // when
        courseQuestion.acceptAnswer(secondAnswer);

        // then
        assertThat(courseQuestion.getAcceptedAnswer()).isEqualTo(secondAnswer);
        assertThat(courseQuestion.getStatus()).isEqualTo(QuestionStatus.ACCEPTED);
        assertThat(firstAnswer.isAccepted()).isFalse();
        assertThat(secondAnswer.isAccepted()).isTrue();
    }

    @Test
    void unacceptAnswer_정상() {
        // given
        CourseQuestion courseQuestion = CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser();
        CourseAnswer answer = CommunityAnswerFixtureBuilder.getCommunityAnswerWithIdAndUser(courseQuestion);
        courseQuestion.acceptAnswer(answer);

        // when
        courseQuestion.unacceptAnswer();

        // then
        assertThat(courseQuestion.getAcceptedAnswer()).isNull();
        assertThat(courseQuestion.getStatus()).isEqualTo(QuestionStatus.ANSWERED);
        assertThat(answer.isAccepted()).isFalse();
    }

    @Test
    void changeStatusByAnswer_답변_있음() {
        // given
        CourseQuestion courseQuestion = CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser();
        
        // when
        courseQuestion.changeStatusByAnswer(true);
        
        // then
        assertThat(courseQuestion.getStatus()).isEqualTo(QuestionStatus.ANSWERED);
    }

    @Test
    void changeStatusByAnswer_답변_없음() {
        // given
        CourseQuestion courseQuestion = CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser();
        courseQuestion.changeStatusByAnswer(true); // 먼저 ANSWERED 상태로 변경
        
        // when
        courseQuestion.changeStatusByAnswer(false);
        
        // then
        assertThat(courseQuestion.getStatus()).isEqualTo(QuestionStatus.WAITING);
    }

    @Test
    void changeStatusByAnswer_채택된답변있음_상태유지() {
        // given
        CourseQuestion courseQuestion = CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser();
        CourseAnswer answer = CommunityAnswerFixtureBuilder.getCommunityAnswerWithIdAndUser(courseQuestion);
        courseQuestion.acceptAnswer(answer); // 답변 채택하여 ACCEPTED 상태로 만듦
        
        // when
        courseQuestion.changeStatusByAnswer(true); // 새 답변 추가 시뮬레이션
        
        // then
        assertThat(courseQuestion.getStatus()).isEqualTo(QuestionStatus.ACCEPTED); // 상태 유지
        assertThat(courseQuestion.getAcceptedAnswer()).isEqualTo(answer); // 채택된 답변 유지
    }
    
    @Test
    void changeStatusByAnswer_채택상태아님_정상변경() {
        // given
        CourseQuestion courseQuestion = CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser();
        // WAITING 상태에서 시작
        
        // when
        courseQuestion.changeStatusByAnswer(true);
        
        // then
        assertThat(courseQuestion.getStatus()).isEqualTo(QuestionStatus.ANSWERED);
    }

    @Test
    void unacceptAnswer_채택된_답변이_없는_경우() {
        // given
        CourseQuestion courseQuestion = CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser();
        
        // when
        courseQuestion.unacceptAnswer();
        
        // then
        assertThat(courseQuestion.getAcceptedAnswer()).isNull();
        assertThat(courseQuestion.getStatus()).isEqualTo(QuestionStatus.ANSWERED);
    }

    @Test
    void handleAcceptedAnswerDeleted_남은답변있음() {
        // given
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();
        User user = UserFixtureBuilder.getUserWithId();
        CourseQuestion courseQuestion = CourseQuestion.create(course, user, "질문", "내용", null);
        
        CourseAnswer acceptedAnswer = CourseAnswer.create(courseQuestion, user, "채택된 답변");
        acceptedAnswer.accept();
        courseQuestion.acceptAnswer(acceptedAnswer);

        // when
        courseQuestion.handleAcceptedAnswerDeleted(true); // 남은 답변 있음

        // then
        assertThat(courseQuestion.getAcceptedAnswer()).isNull();
        assertThat(courseQuestion.getStatus()).isEqualTo(QuestionStatus.ANSWERED);
        assertThat(acceptedAnswer.isAccepted()).isFalse();
    }

    @Test
    void handleAcceptedAnswerDeleted_남은답변없음() {
        // given
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();
        User user = UserFixtureBuilder.getUserWithId();
        CourseQuestion courseQuestion = CourseQuestion.create(course, user, "질문", "내용", null);
        
        CourseAnswer acceptedAnswer = CourseAnswer.create(courseQuestion, user, "채택된 답변");
        acceptedAnswer.accept();
        courseQuestion.acceptAnswer(acceptedAnswer);

        // when
        courseQuestion.handleAcceptedAnswerDeleted(false); // 남은 답변 없음

        // then
        assertThat(courseQuestion.getAcceptedAnswer()).isNull();
        assertThat(courseQuestion.getStatus()).isEqualTo(QuestionStatus.WAITING);
        assertThat(acceptedAnswer.isAccepted()).isFalse();
    }

    @Test
    void handleAcceptedAnswerDeleted_채택된답변없는경우() {
        // given
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();
        User user = UserFixtureBuilder.getUserWithId();
        CourseQuestion courseQuestion = CourseQuestion.create(course, user, "질문", "내용", null);

        // when
        courseQuestion.handleAcceptedAnswerDeleted(true); // 남은 답변 있음

        // then
        assertThat(courseQuestion.getAcceptedAnswer()).isNull();
        assertThat(courseQuestion.getStatus()).isEqualTo(QuestionStatus.ANSWERED);
    }
}
