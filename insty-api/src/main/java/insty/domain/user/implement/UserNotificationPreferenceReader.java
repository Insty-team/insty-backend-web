package insty.domain.user.implement;

import insty.domain.user.repository.UserNotificationPreferenceRepository;
import insty.error.UserErrorCode;
import insty.exception.CustomException;
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
public class UserNotificationPreferenceReader {

    private final UserNotificationPreferenceRepository userNotificationPreferenceRepository;

    public UserNotificationPreference getOrCreateDefaultPreference(User user) {
        return userNotificationPreferenceRepository.findByUser(user)
                .orElseGet(() -> createAndSaveDefaultPreference(user));
    }

    public UserNotificationPreference getPreference(Long userId) {
        return userNotificationPreferenceRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOTIFICATION_PREFERENCE_NOT_FOUND));
    }

    public UserNotificationPreference getPreference(User user) {
        return userNotificationPreferenceRepository.findByUser(user)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOTIFICATION_PREFERENCE_NOT_FOUND));
    }

    public boolean existsByUser(User user) {
        return userNotificationPreferenceRepository.existsByUser(user);
    }

    @Transactional
    private UserNotificationPreference createAndSaveDefaultPreference(User user) {
        UserNotificationPreference defaultPreference = UserNotificationPreference.createDefault(user);
        return userNotificationPreferenceRepository.save(defaultPreference);
    }
}