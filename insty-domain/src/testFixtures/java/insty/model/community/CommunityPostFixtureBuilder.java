package insty.model.community;

import insty.model.course.Course;
import insty.model.course.CourseFixtureBuilder;
import insty.model.user.User;
import insty.model.user.UserFixtureBuilder;
import org.springframework.test.util.ReflectionTestUtils;

public class CommunityPostFixtureBuilder {

    public static CommunityPost getCommunityPostWithIdAndUser() {
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();
        User user = UserFixtureBuilder.getUserWithId(2L); // 수강생 역할
        CommunityPost communityPost = CommunityPostFixture.getCommunityPost(user, course);
        ReflectionTestUtils.setField(communityPost, "id", 1L);
        return communityPost;
    }

    public static CommunityPost getCommunityPostWithIdAndUser(User user, Course course, Long postId, String title, String content) {
        CommunityPost communityPost = CommunityPostFixture.getCommunityPost(user, course, title, content);
        ReflectionTestUtils.setField(communityPost, "id", postId);
        return communityPost;
    }
}
