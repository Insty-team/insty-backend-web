package insty.model.community;

import insty.model.course.Course;
import insty.model.course.CourseFixtureBuilder;
import insty.model.user.User;
import insty.model.user.UserFixtureBuilder;
import org.springframework.test.util.ReflectionTestUtils;

public class CommunityQuestionFixtureBuilder {

    public static CommunityQuestion getCommunityQuestionWithIdAndUser() {
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();
        User user = UserFixtureBuilder.getUserWithId();
        CommunityQuestion communityQuestion = CommunityQuestionFixture.getCommunityQuestion(course, user);
        ReflectionTestUtils.setField(communityQuestion, "id", 1L);
        return communityQuestion;
    }

    public static CommunityQuestion getCommunityQuestionWithIdAndUser(Long questionId, String title, String content) {
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();
        User user = UserFixtureBuilder.getUserWithId();
        CommunityQuestion communityQuestion = CommunityQuestionFixture.getCommunityQuestion(course, user, title, content);
        ReflectionTestUtils.setField(communityQuestion, "id", questionId);
        return communityQuestion;
    }
}