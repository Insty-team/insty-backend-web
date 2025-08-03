package insty.model.community;

import insty.model.user.User;
import insty.model.user.UserFixtureBuilder;
import org.springframework.test.util.ReflectionTestUtils;

public class CommunityAnswerFixtureBuilder {

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