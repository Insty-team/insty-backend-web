package insty.domain.notification.service;

import insty.domain.user.event.UserCreatedEvent;
import insty.model.user.User;
import insty.model.notification.UserNotificationSetting;
import insty.domain.notification.repository.UserNotificationSettingRepository;
import insty.notification.NotificationChannel;
import insty.notification.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 알림 수신 설정 서비스
 *
 * 주요 기능:
 * 1. 사용자별 알림 수신 여부 확인
 * 2. 설정 초기화 (신규 사용자 또는 새 알림 타입 추가 시)
 * 3. 설정 변경
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationPreferenceService {

    private final UserNotificationSettingRepository settingRepository;

    /**
     * 특정 알림 타입과 채널에 대한 수신 허용 여부 확인
     *
     * @param userId 사용자 ID
     * @param type 알림 타입
     * @param channel 알림 채널
     * @return 수신 허용 여부 (설정이 없으면 기본값 true)
     */
    @Transactional(readOnly = true)
    public boolean isNotificationEnabled(Long userId, NotificationType type, NotificationChannel channel) {
        return settingRepository
                .findByUserIdAndNotificationTypeAndChannel(userId, type, channel)
                .map(UserNotificationSetting::isEnabled)
                .orElse(true); // 설정이 없으면 기본적으로 허용
    }

    /**
     * 이메일 수신 허용 여부 확인 (이메일 동의 여부도 함께 체크)
     *
     * @param user 사용자 엔티티
     * @param type 알림 타입
     * @return 이메일 수신 허용 여부
     */
    @Transactional(readOnly = true)
    public boolean isEmailEnabled(User user, NotificationType type) {
        if (!user.isEmailAgreed()) {
            return false; // 이메일 수신 동의하지 않은 경우
        }

        return isNotificationEnabled(user.getId(), type, NotificationChannel.EMAIL);
    }

    /**
     * 사용자의 모든 알림 설정 조회 (Map 형태로 반환)
     * 사용자가 설정 가능한 알림 타입만 반환 (INFO 제외)
     *
     * @param userId 사용자 ID
     * @return Map<NotificationType, Map<NotificationChannel, Boolean>>
     */
    @Transactional(readOnly = true)
    public Map<NotificationType, Map<NotificationChannel, Boolean>> getUserSettings(Long userId) {
        List<UserNotificationSetting> settings = settingRepository.findByUserId(userId);

        Map<NotificationType, Map<NotificationChannel, Boolean>> result = new EnumMap<>(NotificationType.class);

        // 사용자가 설정 가능한 알림 타입만 조회
        for (NotificationType type : NotificationType.getUserConfigurableTypes()) {
            Map<NotificationChannel, Boolean> channelMap = new EnumMap<>(NotificationChannel.class);

            for (NotificationChannel channel : NotificationChannel.values()) {
                boolean enabled = settings.stream()
                        .filter(s -> s.getNotificationType() == type && s.getChannel() == channel)
                        .findFirst()
                        .map(UserNotificationSetting::isEnabled)
                        .orElse(true); // 기본값

                channelMap.put(channel, enabled);
            }

            result.put(type, channelMap);
        }

        return result;
    }

    /**
     * 신규 사용자 알림 설정 초기화
     * 모든 알림 타입 및 채널에 대해 기본값(활성화) 설정 생성
     *
     * @param user 사용자 엔티티
     */
    @Transactional
    public void initializeDefaultSettings(User user) {
        for (NotificationType type : NotificationType.values()) {
            for (NotificationChannel channel : NotificationChannel.values()) {
                UserNotificationSetting setting = UserNotificationSetting.createDefault(user, type, channel);
                settingRepository.save(setting);
            }
        }

        log.info("사용자 알림 설정 초기화 완료 - userId: {}, 설정 수: {}",
                user.getId(), NotificationType.values().length * NotificationChannel.values().length);
    }

    /**
     * 특정 알림 타입 및 채널의 수신 여부 변경
     *
     * @param userId 사용자 ID
     * @param type 알림 타입
     * @param channel 알림 채널
     * @param enabled 활성화 여부
     */
    @Transactional
    public void updateSetting(Long userId, NotificationType type, NotificationChannel channel, boolean enabled) {
        UserNotificationSetting setting = settingRepository
                .findByUserIdAndNotificationTypeAndChannel(userId, type, channel)
                .orElseThrow(() -> new IllegalStateException(
                        String.format("알림 설정을 찾을 수 없습니다 - userId: %d, type: %s, channel: %s",
                                userId, type, channel)
                ));

        setting.updateEnabled(enabled);
        log.debug("알림 설정 변경 - userId: {}, type: {}, channel: {}, enabled: {}",
                userId, type, channel, enabled);
    }

    /**
     * 일괄 설정 변경
     *
     * @param userId 사용자 ID
     * @param type 알림 타입
     * @param inAppEnabled 인앱 알림 활성화 여부
     * @param emailEnabled 이메일 활성화 여부
     */
    @Transactional
    public void updateSettingsForType(Long userId, NotificationType type, boolean inAppEnabled, boolean emailEnabled) {
        updateSetting(userId, type, NotificationChannel.IN_APP, inAppEnabled);
        updateSetting(userId, type, NotificationChannel.EMAIL, emailEnabled);
    }

    /**
     * 모든 알림 끄기 / 켜기
     *
     * @param userId 사용자 ID
     * @param enableAll true: 모두 켜기, false: 모두 끄기
     */
    @Transactional
    public void toggleAllNotifications(Long userId, boolean enableAll) {
        List<UserNotificationSetting> settings = settingRepository.findByUserId(userId);

        settings.forEach(setting -> setting.updateEnabled(enableAll));

        log.info("사용자 모든 알림 일괄 변경 - userId: {}, enableAll: {}", userId, enableAll);
    }

    /**
     * User 생성 이벤트 리스너
     * 트랜잭션 커밋 후 알림 설정 초기화
     *
     * @param event User 생성 이벤트
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserCreatedEvent(UserCreatedEvent event) {
        User user = event.user();
        initializeDefaultSettings(user);
        log.info("User 생성 이벤트 처리 완료 - userId: {}", user.getId());
    }
}
