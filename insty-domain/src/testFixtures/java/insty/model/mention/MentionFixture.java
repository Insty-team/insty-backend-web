package insty.model.mention;

import insty.model.courseqna.CommunityAnswerFixture;
import insty.model.courseqna.CommunityQuestionFixtureBuilder;
import insty.model.courseqna.CourseAnswer;
import insty.model.courseqna.CourseQuestion;
import insty.model.user.User;
import insty.model.user.UserFixtureBuilder;

public class MentionFixture {

    public static Mention getMention() {
        CourseQuestion question = CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser();
        User user = UserFixtureBuilder.getUserWithId();
        CourseAnswer answer = CommunityAnswerFixture.getCommunityAnswer(question, user);
        User mentionedUser = UserFixtureBuilder.getUserWithId(2L);
        User mentionerUser = UserFixtureBuilder.getUserWithId(3L);
        
        return Mention.create(MentionTargetType.COURSE_ANSWER, 1L, mentionedUser, mentionerUser);
    }

    public static Mention getMention(CourseAnswer courseAnswer, User mentionedUser, User mentionerUser) {
        return Mention.create(MentionTargetType.COURSE_ANSWER, 1L, mentionedUser, mentionerUser);
    }

    public static Mention getMention(Long mentionedUserId, Long mentionerUserId) {
        CourseQuestion question = CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser();
        User mentionedUser = UserFixtureBuilder.getUserWithId(mentionedUserId);
        User mentionerUser = UserFixtureBuilder.getUserWithId(mentionerUserId);
        CourseAnswer answer = CommunityAnswerFixture.getCommunityAnswer(question, mentionedUser);
        
        return Mention.create(MentionTargetType.COURSE_ANSWER, 1L, mentionedUser, mentionerUser);
    }
}
