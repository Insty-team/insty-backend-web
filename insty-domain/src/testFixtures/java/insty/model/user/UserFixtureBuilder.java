package insty.model.user;

import org.springframework.test.util.ReflectionTestUtils;

public class UserFixtureBuilder {

    public static User getUserWithId() {
        User user = UserFixture.getUser();
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }

    public static User getUserWithId(Long userId) {
        User user = UserFixture.getUser();
        ReflectionTestUtils.setField(user, "id", userId);
        return user;
    }

    public static User getUserWithId(Long userId, String email, String password, String nickname) {
        User user = UserFixture.getUser(email, password, nickname);
        ReflectionTestUtils.setField(user, "id", userId);
        return user;
    }
}
