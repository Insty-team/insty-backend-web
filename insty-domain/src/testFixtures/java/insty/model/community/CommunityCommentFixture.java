package insty.model.community;

import insty.model.user.User;

public class CommunityCommentFixture {

    public static CommunityComment getCommunityComment(CommunityPost communityPost, User user) {
        return CommunityComment.create(communityPost, user, "댓글 내용");
    }

    public static CommunityComment getCommunityComment(CommunityPost communityPost, User user, String content) {
        return CommunityComment.create(communityPost, user, content);
    }
}
