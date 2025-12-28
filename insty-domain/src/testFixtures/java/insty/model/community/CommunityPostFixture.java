package insty.model.community;

import insty.model.user.User;

public class CommunityPostFixture {

    public static CommunityPost getCommunityPost(User user) {
        return CommunityPost.create(user, "게시글 제목", "게시글 내용");
    }

    public static CommunityPost getCommunityPost(User user, String title, String content) {
        return CommunityPost.create(user, title, content);
    }
}
