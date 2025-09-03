package insty.domain.user.implement;

import insty.domain.user.repository.UserNotificationPreferenceRepository;
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

    private final UserNotificationPreferenceRepository userNotificationPreferenceRepository;

    public boolean shouldReceiveUserMentionEmail(User user) {
        UserNotificationPreference preference = getOrCreateDefaultPreference(user);
        return preference.shouldReceiveUserMentionEmail();
    }

    public boolean shouldReceiveNewQuestionEmail(User user) {
        UserNotificationPreference preference = getOrCreateDefaultPreference(user);
        return preference.shouldReceiveNewQuestionEmail();
    }

    public boolean shouldReceiveNewAnswerEmail(User user) {
        UserNotificationPreference preference = getOrCreateDefaultPreference(user);
        return preference.shouldReceiveNewAnswerEmail();
    }

    public boolean shouldReceiveAnswerAcceptedEmail(User user) {
        UserNotificationPreference preference = getOrCreateDefaultPreference(user);
        return preference.shouldReceiveAnswerAcceptedEmail();
    }

    public boolean shouldReceiveUserMentionNotification(User user) {
        UserNotificationPreference preference = getOrCreateDefaultPreference(user);
        return preference.isUserMentionNotificationEnabled();
    }

    public boolean shouldReceiveNewQuestionNotification(User user) {
        UserNotificationPreference preference = getOrCreateDefaultPreference(user);
        return preference.isNewQuestionNotificationEnabled();
    }

    public boolean shouldReceiveNewAnswerNotification(User user) {
        UserNotificationPreference preference = getOrCreateDefaultPreference(user);
        return preference.isNewAnswerNotificationEnabled();
    }

    public boolean shouldReceiveAnswerAcceptedNotification(User user) {
        UserNotificationPreference preference = getOrCreateDefaultPreference(user);
        return preference.isAnswerAcceptedNotificationEnabled();
    }

    @Transactional
    private UserNotificationPreference getOrCreateDefaultPreference(User user) {
        return userNotificationPreferenceRepository.findByUser(user)
                .orElseGet(() -> createAndSaveDefaultPreference(user));
    }

    private UserNotificationPreference createAndSaveDefaultPreference(User user) {
        UserNotificationPreference defaultPreference = UserNotificationPreference.createDefault(user);
        return userNotificationPreferenceRepository.save(defaultPreference);
    }
}