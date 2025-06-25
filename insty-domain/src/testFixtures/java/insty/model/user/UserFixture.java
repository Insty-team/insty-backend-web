package insty.model.user;

public class UserFixture {

    public static User getUser() {
        return User.create("example@example.com", "example12!@", "example");
    }

    public static User getUser(String email, String password, String nickname) {
        return User.create(email, password, nickname);
    }
}
