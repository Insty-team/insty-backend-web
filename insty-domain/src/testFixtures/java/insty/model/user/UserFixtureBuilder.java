package insty.model.user;

import org.springframework.test.util.ReflectionTestUtils;

public class UserFixtureBuilder {

    public static User getUserWithId() {
        User user = UserFixture.getUser();
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }
}
