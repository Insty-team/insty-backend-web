package insty.domain.notification.service;

import insty.domain.user.repository.UserRepository;
import insty.model.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * UserNotificationSetting 마이그레이션 서비스
 * 기존 User들에게 알림 설정을 초기화하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserNotificationSettingMigrationService {

    private final UserRepository userRepository;
    private final NotificationPreferenceService preferenceService;

    /**
     * 알림 설정이 없는 모든 사용자에 대해 기본 설정 초기화
     *
     * @return 초기화된 사용자 수
     */
    @Transactional
    public int migrateAllUsersWithoutSettings() {
        log.info("UserNotificationSetting 마이그레이션 시작");

        List<User> allUsers = userRepository.findAll();
        int migratedCount = 0;

        for (User user : allUsers) {
            // 이미 설정이 있는지 확인
            boolean hasSettings = !preferenceService.getUserSettings(user.getId()).isEmpty();

            if (!hasSettings) {
                try {
                    preferenceService.initializeDefaultSettings(user);
                    migratedCount++;
                    log.debug("사용자 알림 설정 초기화 완료 - userId: {}", user.getId());
                } catch (Exception e) {
                    log.error("사용자 알림 설정 초기화 실패 - userId: {}", user.getId(), e);
                }
            }
        }

        log.info("UserNotificationSetting 마이그레이션 완료 - 총 사용자: {}, 초기화된 사용자: {}",
                allUsers.size(), migratedCount);

        return migratedCount;
    }

    /**
     * 특정 사용자에 대해 강제로 기본 설정 초기화 (기존 설정 삭제 후 재생성)
     *
     * @param userId 사용자 ID
     */
    @Transactional
    public void resetUserSettings(Long userId) {
        log.info("사용자 알림 설정 리셋 시작 - userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        preferenceService.initializeDefaultSettings(user);

        log.info("사용자 알림 설정 리셋 완료 - userId: {}", userId);
    }
}
