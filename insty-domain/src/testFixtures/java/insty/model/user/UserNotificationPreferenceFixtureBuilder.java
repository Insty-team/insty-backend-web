package insty.model.user;

import org.springframework.test.util.ReflectionTestUtils;

public class UserNotificationPreferenceFixtureBuilder {

    public static UserNotificationPreference getPreferenceWithId() {
        UserNotificationPreference preference = UserNotificationPreferenceFixture.getDefaultPreference();
        ReflectionTestUtils.setField(preference, "id", 1L);
        return preference;
    }

    public static UserNotificationPreference getPreferenceWithId(Long preferenceId) {
        UserNotificationPreference preference = UserNotificationPreferenceFixture.getDefaultPreference();
        ReflectionTestUtils.setField(preference, "id", preferenceId);
        return preference;
    }

    public static UserNotificationPreference getPreferenceWithId(Long preferenceId, User user) {
        UserNotificationPreference preference = UserNotificationPreferenceFixture.getDefaultPreference(user);
        ReflectionTestUtils.setField(preference, "id", preferenceId);
        return preference;
    }
}