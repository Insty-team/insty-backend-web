package insty.domain.notification.dto;

import insty.notification.NotificationChannel;
import insty.notification.NotificationType;

import java.util.Map;

/**
 * 사용자 알림 설정 조회 응답 DTO
 */
public record UserNotificationSettingResponse(
        Map<NotificationType, NotificationChannelSettings> settings
) {
    /**
     * 알림 타입별 채널 설정
     */
    public record NotificationChannelSettings(
            boolean inAppEnabled,
            boolean emailEnabled
    ) {
    }

    /**
     * NotificationPreferenceService의 Map 결과를 DTO로 변환
     */
    public static UserNotificationSettingResponse from(Map<NotificationType, Map<NotificationChannel, Boolean>> settingsMap) {
        Map<NotificationType, NotificationChannelSettings> transformed = settingsMap.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            Map<NotificationChannel, Boolean> channels = entry.getValue();
                            return new NotificationChannelSettings(
                                    channels.getOrDefault(NotificationChannel.IN_APP, true),
                                    channels.getOrDefault(NotificationChannel.EMAIL, true)
                            );
                        }
                ));

        return new UserNotificationSettingResponse(transformed);
    }
}
