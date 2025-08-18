package insty.model.community;

import insty.domain.common.dto.UserInfo;
import insty.domain.community.dto.CommunityQuestionSearchInfo;
import insty.model.course.Course;
import insty.model.course.CourseFixtureBuilder;
import insty.model.user.User;
import insty.model.user.UserFixtureBuilder;
import insty.model.user.UserType;
import java.time.Instant;
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

    public static CommunityQuestionSearchInfo getCommunityQuestionSearchInfo(Long questionId, Long courseId, String title, String content) {
        UserInfo userInfo = new UserInfo(1L, "테스트 유저", UserType.LEARNER);
        return new CommunityQuestionSearchInfo(
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

    public static CommunityQuestionSearchInfo getCommunityQuestionSearchInfo() {
        return getCommunityQuestionSearchInfo(1L, 1L, "질문 제목", "질문 내용");
    }
}