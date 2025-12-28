package insty.model.community;

import insty.model.user.User;
import insty.model.user.UserFixtureBuilder;
import org.springframework.test.util.ReflectionTestUtils;

public class CommunityPostFixtureBuilder {

    public static CommunityPost getCommunityPostWithIdAndUser() {
        User user = UserFixtureBuilder.getUserWithId();
        CommunityPost communityPost = CommunityPostFixture.getCommunityPost(user);
        ReflectionTestUtils.setField(communityPost, "id", 1L);
        return communityPost;
    }

    public static CommunityPost getCommunityPostWithIdAndUser(User user, Long postId, String title, String content) {
        CommunityPost communityPost = CommunityPostFixture.getCommunityPost(user, title, content);
        ReflectionTestUtils.setField(communityPost, "id", postId);
        return communityPost;
    }
}
