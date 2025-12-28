package insty.model.courseqna;

import insty.model.course.Course;
import insty.model.user.User;
import insty.model.user.UserFixtureBuilder;
import org.springframework.test.util.ReflectionTestUtils;

public class CommunityAnswerFixtureBuilder {

    public static CourseAnswer getCommunityAnswerWithId(Long answerId) {
        User user = UserFixtureBuilder.getUserWithId();
        Course course = Course.create(user, "테스트 강의", "테스트 강의 설명", 10000, "테스트 대상자", true);
        ReflectionTestUtils.setField(course, "id", 1L);
        
        CourseQuestion question = CommunityQuestionFixture.getCommunityQuestion(course, user);
        CourseAnswer courseAnswer = CommunityAnswerFixture.getCommunityAnswer(question, user);
        ReflectionTestUtils.setField(courseAnswer, "id", answerId);
        return courseAnswer;
    }

    public static CourseAnswer getCommunityAnswerWithIdAndUser(CourseQuestion courseQuestion) {
        User user = UserFixtureBuilder.getUserWithId();
        CourseAnswer courseAnswer = CommunityAnswerFixture.getCommunityAnswer(courseQuestion, user);
        ReflectionTestUtils.setField(courseAnswer, "id", 1L);
        return courseAnswer;
    }

    public static CourseAnswer getCommunityAnswerWithIdAndUser(CourseQuestion courseQuestion, Long answerId, String content) {
        User user = UserFixtureBuilder.getUserWithId();
        CourseAnswer courseAnswer = CommunityAnswerFixture.getCommunityAnswer(courseQuestion, user, content);
        ReflectionTestUtils.setField(courseAnswer, "id", answerId);
        return courseAnswer;
    }
}