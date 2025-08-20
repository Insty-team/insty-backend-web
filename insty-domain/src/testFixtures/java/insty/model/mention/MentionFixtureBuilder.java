package insty.model.mention;

import insty.model.community.CommunityAnswer;
import insty.model.community.CommunityAnswerFixtureBuilder;
import insty.model.community.CommunityQuestion;
import insty.model.community.CommunityQuestionFixture;
import insty.model.user.User;
import insty.model.user.UserFixtureBuilder;
import org.springframework.test.util.ReflectionTestUtils;

public class MentionFixtureBuilder {

    public static Mention getMentionWithId() {
        Mention mention = MentionFixture.getMention();
        ReflectionTestUtils.setField(mention, "id", 1L);
        return mention;
    }

    public static Mention getMentionWithId(Long mentionId) {
        Mention mention = MentionFixture.getMention();
        ReflectionTestUtils.setField(mention, "id", mentionId);
        return mention;
    }

    public static Mention getMentionWithId(Long mentionId, Long mentionedUserId, Long mentionerUserId) {
        Mention mention = MentionFixture.getMention(mentionedUserId, mentionerUserId);
        ReflectionTestUtils.setField(mention, "id", mentionId);
        return mention;
    }

    public static Mention getMentionWithId(Long mentionId, CommunityAnswer communityAnswer, User mentionedUser, User mentionerUser) {
        Mention mention = MentionFixture.getMention(communityAnswer, mentionedUser, mentionerUser);
        ReflectionTestUtils.setField(mention, "id", mentionId);
        return mention;
    }
}
