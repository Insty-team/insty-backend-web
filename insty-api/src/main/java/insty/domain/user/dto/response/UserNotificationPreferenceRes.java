package insty.domain.user.dto.response;

import insty.model.user.UserNotificationPreference;

public record UserNotificationPreferenceRes(
        Long id,
        Long userId,
        boolean userMentionNotificationEnabled,
        boolean userMentionEmailEnabled,
        boolean newQuestionNotificationEnabled,
        boolean newQuestionEmailEnabled,
        boolean newAnswerNotificationEnabled,
        boolean newAnswerEmailEnabled,
        boolean answerAcceptedNotificationEnabled,
        boolean answerAcceptedEmailEnabled
) {
    public static UserNotificationPreferenceRes from(UserNotificationPreference preference) {
        return new UserNotificationPreferenceRes(
                preference.getId(),
                preference.getUser().getId(),
                preference.isUserMentionNotificationEnabled(),
                preference.isUserMentionEmailEnabled(),
                preference.isNewQuestionNotificationEnabled(),
                preference.isNewQuestionEmailEnabled(),
                preference.isNewAnswerNotificationEnabled(),
                preference.isNewAnswerEmailEnabled(),
                preference.isAnswerAcceptedNotificationEnabled(),
                preference.isAnswerAcceptedEmailEnabled()
        );
    }
}