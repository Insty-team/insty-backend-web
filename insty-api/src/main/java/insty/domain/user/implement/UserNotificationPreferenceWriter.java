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
@Transactional
public class UserNotificationPreferenceWriter {

    private final UserNotificationPreferenceRepository userNotificationPreferenceRepository;

    public UserNotificationPreference createDefaultPreference(User user) {
        UserNotificationPreference preference = UserNotificationPreference.createDefault(user);
        return userNotificationPreferenceRepository.save(preference);
    }

    public UserNotificationPreference updateUserMentionSettings(UserNotificationPreference preference, 
                                                               boolean notificationEnabled, 
                                                               boolean emailEnabled) {
        preference.updateUserMentionSettings(notificationEnabled, emailEnabled);
        return userNotificationPreferenceRepository.save(preference);
    }

    public UserNotificationPreference updateNewQuestionSettings(UserNotificationPreference preference, 
                                                              boolean notificationEnabled, 
                                                              boolean emailEnabled) {
        preference.updateNewQuestionSettings(notificationEnabled, emailEnabled);
        return userNotificationPreferenceRepository.save(preference);
    }

    public UserNotificationPreference updateNewAnswerSettings(UserNotificationPreference preference, 
                                                            boolean notificationEnabled, 
                                                            boolean emailEnabled) {
        preference.updateNewAnswerSettings(notificationEnabled, emailEnabled);
        return userNotificationPreferenceRepository.save(preference);
    }

    public UserNotificationPreference updateAnswerAcceptedSettings(UserNotificationPreference preference, 
                                                                 boolean notificationEnabled, 
                                                                 boolean emailEnabled) {
        preference.updateAnswerAcceptedSettings(notificationEnabled, emailEnabled);
        return userNotificationPreferenceRepository.save(preference);
    }
}