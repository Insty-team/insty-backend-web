package insty.model.mention;

import insty.model.community.CommunityAnswer;
import insty.model.community.CommunityAnswerFixture;
import insty.model.community.CommunityQuestion;
import insty.model.community.CommunityQuestionFixtureBuilder;
import insty.model.user.User;
import insty.model.user.UserFixtureBuilder;

public class MentionFixture {

    public static Mention getMention() {
        CommunityQuestion question = CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser();
        User user = UserFixtureBuilder.getUserWithId();
        CommunityAnswer answer = CommunityAnswerFixture.getCommunityAnswer(question, user);
        User mentionedUser = UserFixtureBuilder.getUserWithId(2L);
        User mentionerUser = UserFixtureBuilder.getUserWithId(3L);
        
        return Mention.create(answer, mentionedUser, mentionerUser);
    }

    public static Mention getMention(CommunityAnswer communityAnswer, User mentionedUser, User mentionerUser) {
        return Mention.create(communityAnswer, mentionedUser, mentionerUser);
    }

    public static Mention getMention(Long mentionedUserId, Long mentionerUserId) {
        CommunityQuestion question = CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser();
        User mentionedUser = UserFixtureBuilder.getUserWithId(mentionedUserId);
        User mentionerUser = UserFixtureBuilder.getUserWithId(mentionerUserId);
        CommunityAnswer answer = CommunityAnswerFixture.getCommunityAnswer(question, mentionedUser);
        
        return Mention.create(answer, mentionedUser, mentionerUser);
    }
}

