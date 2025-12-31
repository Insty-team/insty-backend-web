package insty.model.community;

import insty.model.course.Course;
import insty.model.user.User;

public class CommunityPostFixture {

    public static CommunityPost getCommunityPost(User user, Course course) {
        return CommunityPost.create(user, course, "게시글 제목", "게시글 내용");
    }

    public static CommunityPost getCommunityPost(User user, Course course, String title, String content) {
        return CommunityPost.create(user, course, title, content);
    }
}
