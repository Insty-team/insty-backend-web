package insty.domain.notification.service;

import insty.domain.notification.repository.UserNotificationSettingRepository;
import insty.domain.user.event.UserCreatedEvent;
import insty.domain.user.repository.UserRepository;
import insty.error.NotificationErrorCode;
import insty.error.UserErrorCode;
import insty.exception.CustomException;
import insty.model.notification.UserNotificationSetting;
import insty.model.user.User;
import insty.notification.NotificationChannel;
import insty.notification.NotificationType;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class NotificationSettingsService {

    private final UserNotificationSettingRepository settingRepository;
    private final UserRepository userRepository;

    /**
     * 특정 알림 타입과 채널에 대한 수신 허용 여부 확인
     */
    @Transactional(readOnly = true)
    public boolean isNotificationEnabled(Long userId, NotificationType type, NotificationChannel channel) {
        return settingRepository
                .findByUserIdAndNotificationTypeAndChannel(userId, type, channel)
                .map(UserNotificationSetting::isEnabled)
                .orElse(true);
    }

    /**
     * 이메일 수신 허용 여부 확인 (이메일 동의 여부도 함께 체크)
     */
    @Transactional(readOnly = true)
    public boolean isEmailEnabled(User user, NotificationType type) {
        if (!user.isEmailAgreed()) {
            return false;
        }

        return isNotificationEnabled(user.getId(), type, NotificationChannel.EMAIL);
    }

    /**
     * 사용자의 알림 설정 존재 여부 확인
     */
    @Transactional(readOnly = true)
    public boolean hasUserSettings(Long userId) {
        List<UserNotificationSetting> settings = settingRepository.findByUserId(userId);
        return !settings.isEmpty();
    }

    /**
     * 사용자의 모든 알림 설정 조회 (Map 형태로 반환)
     */
    @Transactional(readOnly = true)
    public Map<NotificationType, Map<NotificationChannel, Boolean>> getUserSettings(Long userId) {
        List<UserNotificationSetting> settings = settingRepository.findByUserId(userId);

        Map<NotificationType, Map<NotificationChannel, Boolean>> result = new EnumMap<>(NotificationType.class);

        for (NotificationType type : NotificationType.getUserConfigurableTypes()) {
            Map<NotificationChannel, Boolean> channelMap = new EnumMap<>(NotificationChannel.class);

            for (NotificationChannel channel : NotificationChannel.values()) {
                boolean enabled = settings.stream()
                        .filter(s -> s.getNotificationType() == type && s.getChannel() == channel)
                        .findFirst()
                        .map(UserNotificationSetting::isEnabled)
                        .orElse(true);

                channelMap.put(channel, enabled);
            }

            result.put(type, channelMap);
        }

        return result;
    }

    /**
     * 사용자의 알림 설정 조회, 없으면 기본값으로 생성
     */
    @Transactional
    public Map<NotificationType, Map<NotificationChannel, Boolean>> getOrCreateUserSettings(Long userId) {
        List<UserNotificationSetting> settings = settingRepository.findByUserId(userId);

        if (settings.isEmpty()) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
            initializeDefaultSettings(user);
            settings = settingRepository.findByUserId(userId);
        }

        Map<NotificationType, Map<NotificationChannel, Boolean>> result = new EnumMap<>(NotificationType.class);

        for (NotificationType type : NotificationType.getUserConfigurableTypes()) {
            Map<NotificationChannel, Boolean> channelMap = new EnumMap<>(NotificationChannel.class);

            for (NotificationChannel channel : NotificationChannel.values()) {
                boolean enabled = settings.stream()
                        .filter(s -> s.getNotificationType() == type && s.getChannel() == channel)
                        .findFirst()
                        .map(UserNotificationSetting::isEnabled)
                        .orElse(true);

                channelMap.put(channel, enabled);
            }

            result.put(type, channelMap);
        }

        return result;
    }

    /**
     * 신규 사용자 알림 설정 초기화
     */
    @Transactional
    public void initializeDefaultSettings(User user) {
        for (NotificationType type : NotificationType.values()) {
            for (NotificationChannel channel : NotificationChannel.values()) {
                UserNotificationSetting setting = UserNotificationSetting.createDefault(user, type, channel);
                settingRepository.save(setting);
            }
        }
    }

    /**
     * 특정 알림 타입 및 채널의 수신 여부 변경
     */
    @Transactional
    public void updateSetting(Long userId, NotificationType type, NotificationChannel channel, boolean enabled) {
        UserNotificationSetting setting = settingRepository
                .findByUserIdAndNotificationTypeAndChannel(userId, type, channel)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
                    return settingRepository.save(UserNotificationSetting.createDefault(user, type, channel));
                });

        setting.updateEnabled(enabled);
    }

    /**
     * 일괄 설정 변경
     */
    @Transactional
    public void updateSettingsForType(Long userId, NotificationType type, boolean inAppEnabled, boolean emailEnabled) {
        updateSetting(userId, type, NotificationChannel.IN_APP, inAppEnabled);
        updateSetting(userId, type, NotificationChannel.EMAIL, emailEnabled);
    }

    /**
     * 모든 알림 끄기 / 켜기
     */
    @Transactional
    public void toggleAllNotifications(Long userId, boolean enableAll) {
        List<UserNotificationSetting> settings = settingRepository.findByUserId(userId);

        if (settings.isEmpty()) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
            initializeDefaultSettings(user);
            settings = settingRepository.findByUserId(userId);
        }

        settings.forEach(setting -> setting.updateEnabled(enableAll));
    }

    /**
     * User 생성 이벤트 리스너
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserCreatedEvent(UserCreatedEvent event) {
        User user = event.user();
        initializeDefaultSettings(user);
    }
}
