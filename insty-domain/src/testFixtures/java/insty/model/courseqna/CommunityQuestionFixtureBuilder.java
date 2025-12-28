package insty.model.courseqna;

import insty.domain.common.dto.UserInfo;
import insty.domain.courseqna.dto.CourseQuestionSearchInfo;
import insty.model.course.Course;
import insty.model.course.CourseFixtureBuilder;
import insty.model.user.UserType;
import java.time.Instant;
import org.springframework.test.util.ReflectionTestUtils;

public class CommunityQuestionFixtureBuilder {

    public static CourseQuestion getCommunityQuestionWithIdAndUser() {
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();
        CourseQuestion courseQuestion = CommunityQuestionFixture.getCommunityQuestion(course, course.getUser());
        ReflectionTestUtils.setField(courseQuestion, "id", 1L);
        return courseQuestion;
    }

    public static CourseQuestion getCommunityQuestionWithIdAndUser(Long questionId, String title, String content) {
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();
        CourseQuestion courseQuestion = CommunityQuestionFixture.getCommunityQuestion(course, course.getUser(), title, content);
        ReflectionTestUtils.setField(courseQuestion, "id", questionId);
        return courseQuestion;
    }

    public static CourseQuestionSearchInfo getCommunityQuestionSearchInfo(Long questionId, Long courseId, String title, String content) {
        UserInfo userInfo = new UserInfo(1L, "테스트 유저", UserType.LEARNER);
        return new CourseQuestionSearchInfo(
                questionId,
                userInfo,
                courseId,
                title,
                content,
                QuestionStatus.WAITING,
                Instant.now(),
                Instant.now()
        );
    }

    public static CourseQuestionSearchInfo getCommunityQuestionSearchInfo() {
        return getCommunityQuestionSearchInfo(1L, 1L, "질문 제목", "질문 내용");
    }
}