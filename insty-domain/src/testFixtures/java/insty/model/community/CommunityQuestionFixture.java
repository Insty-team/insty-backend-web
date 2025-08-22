package insty.model.community;

import insty.model.course.Course;
import insty.model.course.CourseFixture;
import insty.model.user.User;
import insty.model.user.UserFixtureBuilder;
import org.springframework.test.util.ReflectionTestUtils;

public class CommunityQuestionFixture {

    public static CommunityQuestion getCommunityQuestion() {
        return CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser();
    }

    public static CommunityQuestion getCommunityQuestion(Course course, User user) {
        return CommunityQuestion.create(course, user, "질문 제목", "질문 내용");
    }

    public static CommunityQuestion getCommunityQuestion(Course course, User user, String title, String content) {
        return CommunityQuestion.create(course, user, title, content);
    }
}
