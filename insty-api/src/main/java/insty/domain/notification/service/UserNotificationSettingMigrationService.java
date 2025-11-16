package insty.domain.notification.service;

import insty.domain.user.repository.UserRepository;
import insty.error.UserErrorCode;
import insty.exception.CustomException;
import insty.model.user.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserNotificationSettingMigrationService {

    private final UserRepository userRepository;
    private final NotificationPreferenceService preferenceService;

    /**
        알림 설정이 없는 모든 사용자에 대해 기본 설정 초기화
     */
    @Transactional
    public int migrateAllUsersWithoutSettings() {
        List<User> allUsers = userRepository.findAll();
        int migratedCount = 0;

        for (User user : allUsers) {
            boolean hasSettings = !preferenceService.getUserSettings(user.getId()).isEmpty();

            if (!hasSettings) {
                try {
                    preferenceService.initializeDefaultSettings(user);
                    migratedCount++;
                } catch (Exception e) {
                    log.error("사용자 알림 설정 초기화 실패 - userId: {}", user.getId(), e);
                }
            }
        }

        return migratedCount;
    }

    /**
     * 특정 사용자에 대해 강제로 기본 설정 초기화 (기존 설정 삭제 후 재생성)
     */
    @Transactional
    public void resetUserSettings(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        preferenceService.initializeDefaultSettings(user);
    }
}
