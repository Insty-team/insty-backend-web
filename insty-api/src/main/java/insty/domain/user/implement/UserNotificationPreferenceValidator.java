package insty.domain.user.implement;

import insty.model.user.User;
import insty.model.user.UserNotificationPreference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserNotificationPreferenceValidator {

    private final UserNotificationPreferenceReader userNotificationPreferenceReader;

    public boolean shouldReceiveUserMentionEmail(User user) {
        UserNotificationPreference preference = userNotificationPreferenceReader.getOrCreateDefaultPreference(user);
        return preference.shouldReceiveUserMentionEmail();
    }

    public boolean shouldReceiveNewQuestionEmail(User user) {
        UserNotificationPreference preference = userNotificationPreferenceReader.getOrCreateDefaultPreference(user);
        return preference.shouldReceiveNewQuestionEmail();
    }

    public boolean shouldReceiveNewAnswerEmail(User user) {
        UserNotificationPreference preference = userNotificationPreferenceReader.getOrCreateDefaultPreference(user);
        return preference.shouldReceiveNewAnswerEmail();
    }

    public boolean shouldReceiveAnswerAcceptedEmail(User user) {
        UserNotificationPreference preference = userNotificationPreferenceReader.getOrCreateDefaultPreference(user);
        return preference.shouldReceiveAnswerAcceptedEmail();
    }

    public boolean shouldReceiveUserMentionNotification(User user) {
        UserNotificationPreference preference = userNotificationPreferenceReader.getOrCreateDefaultPreference(user);
        return preference.isUserMentionNotificationEnabled();
    }

    public boolean shouldReceiveNewQuestionNotification(User user) {
        UserNotificationPreference preference = userNotificationPreferenceReader.getOrCreateDefaultPreference(user);
        return preference.isNewQuestionNotificationEnabled();
    }

    public boolean shouldReceiveNewAnswerNotification(User user) {
        UserNotificationPreference preference = userNotificationPreferenceReader.getOrCreateDefaultPreference(user);
        return preference.isNewAnswerNotificationEnabled();
    }

    public boolean shouldReceiveAnswerAcceptedNotification(User user) {
        UserNotificationPreference preference = userNotificationPreferenceReader.getOrCreateDefaultPreference(user);
        return preference.isAnswerAcceptedNotificationEnabled();
    }
}