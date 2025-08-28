package insty.model.user;

public class UserNotificationPreferenceFixture {

    public static UserNotificationPreference getDefaultPreference() {
        User user = UserFixture.getUser();
        return UserNotificationPreference.createDefault(user);
    }

    public static UserNotificationPreference getDefaultPreference(User user) {
        return UserNotificationPreference.createDefault(user);
    }

    public static UserNotificationPreference getCustomPreference(User user, 
                                                                boolean userMentionNotification, 
                                                                boolean userMentionEmail,
                                                                boolean newQuestionNotification,
                                                                boolean newQuestionEmail,
                                                                boolean newAnswerNotification,
                                                                boolean newAnswerEmail,
                                                                boolean answerAcceptedNotification,
                                                                boolean answerAcceptedEmail) {
        return UserNotificationPreference.builder()
                .user(user)
                .userMentionNotificationEnabled(userMentionNotification)
                .userMentionEmailEnabled(userMentionEmail)
                .newQuestionNotificationEnabled(newQuestionNotification)
                .newQuestionEmailEnabled(newQuestionEmail)
                .newAnswerNotificationEnabled(newAnswerNotification)
                .newAnswerEmailEnabled(newAnswerEmail)
                .answerAcceptedNotificationEnabled(answerAcceptedNotification)
                .answerAcceptedEmailEnabled(answerAcceptedEmail)
                .build();
    }

    public static UserNotificationPreference getAllDisabledPreference(User user) {
        return getCustomPreference(user, false, false, false, false, false, false, false, false);
    }

    public static UserNotificationPreference getOnlyEmailEnabledPreference(User user) {
        return getCustomPreference(user, false, true, false, true, false, true, false, true);
    }
}