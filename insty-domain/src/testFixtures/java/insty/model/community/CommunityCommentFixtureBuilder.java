package insty.model.community;

import insty.model.user.User;
import insty.model.user.UserFixtureBuilder;
import org.springframework.test.util.ReflectionTestUtils;

public class CommunityCommentFixtureBuilder {

    public static CommunityComment getCommunityCommentWithIdAndUser(CommunityPost communityPost) {
        User user = UserFixtureBuilder.getUserWithId();
        CommunityComment communityComment = CommunityCommentFixture.getCommunityComment(communityPost, user);
        ReflectionTestUtils.setField(communityComment, "id", 1L);
        return communityComment;
    }

    public static CommunityComment getCommunityCommentWithIdAndUser(CommunityPost communityPost, Long commentId, String content) {
        User user = UserFixtureBuilder.getUserWithId();
        CommunityComment communityComment = CommunityCommentFixture.getCommunityComment(communityPost, user, content);
        ReflectionTestUtils.setField(communityComment, "id", commentId);
        return communityComment;
    }
}
