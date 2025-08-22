package insty.model.community;

import insty.model.course.Course;
import insty.model.user.User;
import insty.model.user.UserFixtureBuilder;
import org.springframework.test.util.ReflectionTestUtils;

public class CommunityAnswerFixtureBuilder {

    public static CommunityAnswer getCommunityAnswerWithId(Long answerId) {
        User user = UserFixtureBuilder.getUserWithId();
        Course course = Course.create(user, "테스트 강의", "테스트 강의 설명", 10000, "테스트 대상자", true);
        ReflectionTestUtils.setField(course, "id", 1L);
        
        CommunityQuestion question = CommunityQuestionFixture.getCommunityQuestion(course, user);
        CommunityAnswer communityAnswer = CommunityAnswerFixture.getCommunityAnswer(question, user);
        ReflectionTestUtils.setField(communityAnswer, "id", answerId);
        return communityAnswer;
    }

    public static CommunityAnswer getCommunityAnswerWithIdAndUser(CommunityQuestion communityQuestion) {
        User user = UserFixtureBuilder.getUserWithId();
        CommunityAnswer communityAnswer = CommunityAnswerFixture.getCommunityAnswer(communityQuestion, user);
        ReflectionTestUtils.setField(communityAnswer, "id", 1L);
        return communityAnswer;
    }

    public static CommunityAnswer getCommunityAnswerWithIdAndUser(CommunityQuestion communityQuestion, Long answerId, String content) {
        User user = UserFixtureBuilder.getUserWithId();
        CommunityAnswer communityAnswer = CommunityAnswerFixture.getCommunityAnswer(communityQuestion, user, content);
        ReflectionTestUtils.setField(communityAnswer, "id", answerId);
        return communityAnswer;
    }
}