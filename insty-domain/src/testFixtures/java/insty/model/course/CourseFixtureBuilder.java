package insty.model.course;

import insty.model.user.User;
import insty.model.user.UserFixtureBuilder;
import org.springframework.test.util.ReflectionTestUtils;

public class CourseFixtureBuilder {

    public static Course getCourseWithIdAndUser() {
        User user = UserFixtureBuilder.getUserWithId();
        Course course = CourseFixture.getCourse(user);
        ReflectionTestUtils.setField(course, "id", 1L);
        return course;
    }
}
