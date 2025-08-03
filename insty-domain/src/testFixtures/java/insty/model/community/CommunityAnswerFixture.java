package insty.model.community;

import insty.model.user.User;

public class CommunityAnswerFixture {

    public static CommunityAnswer getCommunityAnswer(CommunityQuestion communityQuestion, User user) {
        return CommunityAnswer.create(communityQuestion, user, "답변 내용");
    }

    public static CommunityAnswer getCommunityAnswer(CommunityQuestion communityQuestion, User user, String content) {
        return CommunityAnswer.create(communityQuestion, user, content);
    }
}
