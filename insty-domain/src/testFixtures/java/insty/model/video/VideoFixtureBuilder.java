package insty.model.video;

import insty.model.user.User;
import insty.model.user.UserFixtureBuilder;
import org.springframework.test.util.ReflectionTestUtils;

public class VideoFixtureBuilder {

    public static VideoCourse getVideoCourseWithId() {
        User user = UserFixtureBuilder.getUserWithId();
        VideoCourse videoCourse = VideoCourseFixture.getVideoCourse(user);
        ReflectionTestUtils.setField(videoCourse, "id", 1L);
        return videoCourse;
    }
}
