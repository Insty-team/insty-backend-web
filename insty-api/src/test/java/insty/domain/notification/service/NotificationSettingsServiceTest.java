package insty.domain.notification.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import insty.domain.notification.repository.UserNotificationSettingRepository;
import insty.domain.user.event.UserCreatedEvent;
import insty.domain.user.repository.UserRepository;
import insty.error.NotificationErrorCode;
import insty.exception.CustomException;
import insty.model.notification.UserNotificationSetting;
import insty.model.user.User;
import insty.notification.NotificationChannel;
import insty.notification.NotificationType;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationSettingsServiceTest {

    @InjectMocks
    private NotificationSettingsService notificationSettingsService;

    @Mock
    private UserNotificationSettingRepository settingRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    void 알림_수신_허용_확인_설정_있음() {
        // Given
        Long userId = 1L;
        NotificationType type = NotificationType.NEW_COMMUNITY_QUESTION;
        NotificationChannel channel = NotificationChannel.IN_APP;

        User user = User.builder().id(userId).build();
        UserNotificationSetting setting = UserNotificationSetting.createDefault(user, type, channel);
        setting.updateEnabled(true);

        when(settingRepository.findByUserIdAndNotificationTypeAndChannel(userId, type, channel))
                .thenReturn(Optional.of(setting));

        // When
        boolean result = notificationSettingsService.isNotificationEnabled(userId, type, channel);

        // Then
        assertTrue(result);
    }

    @Test
    void 알림_수신_허용_확인_설정_없음_기본값_true() {
        // Given
        Long userId = 1L;
        NotificationType type = NotificationType.NEW_COMMUNITY_QUESTION;
        NotificationChannel channel = NotificationChannel.IN_APP;

        when(settingRepository.findByUserIdAndNotificationTypeAndChannel(userId, type, channel))
                .thenReturn(Optional.empty());

        // When
        boolean result = notificationSettingsService.isNotificationEnabled(userId, type, channel);

        // Then
        assertTrue(result);
    }

    @Test
    void 이메일_수신_허용_확인_이메일_동의_안함() {
        // Given
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .isEmailAgreed(false)
                .build();

        NotificationType type = NotificationType.NEW_COMMUNITY_QUESTION;

        // When
        boolean result = notificationSettingsService.isEmailEnabled(user, type);

        // Then
        assertFalse(result);
    }

    @Test
    void 이메일_수신_허용_확인_이메일_동의함_설정_허용() {
        // Given
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .isEmailAgreed(true)
                .build();

        NotificationType type = NotificationType.NEW_COMMUNITY_QUESTION;
        NotificationChannel channel = NotificationChannel.EMAIL;

        UserNotificationSetting setting = UserNotificationSetting.createDefault(user, type, channel);
        setting.updateEnabled(true);

        when(settingRepository.findByUserIdAndNotificationTypeAndChannel(userId, type, channel))
                .thenReturn(Optional.of(setting));

        // When
        boolean result = notificationSettingsService.isEmailEnabled(user, type);

        // Then
        assertTrue(result);
    }

    @Test
    void 사용자_모든_알림_설정_조회() {
        // Given
        Long userId = 1L;
        User user = User.builder().id(userId).build();

        UserNotificationSetting setting1 = UserNotificationSetting.createDefault(
                user, NotificationType.NEW_COMMUNITY_QUESTION, NotificationChannel.IN_APP);
        setting1.updateEnabled(true);

        UserNotificationSetting setting2 = UserNotificationSetting.createDefault(
                user, NotificationType.NEW_COMMUNITY_QUESTION, NotificationChannel.EMAIL);
        setting2.updateEnabled(false);

        UserNotificationSetting setting3 = UserNotificationSetting.createDefault(
                user, NotificationType.NEW_COMMUNITY_ANSWER, NotificationChannel.IN_APP);
        setting3.updateEnabled(true);

        List<UserNotificationSetting> settings = List.of(setting1, setting2, setting3);

        when(settingRepository.findByUserId(userId)).thenReturn(settings);

        // When
        Map<NotificationType, Map<NotificationChannel, Boolean>> result =
                notificationSettingsService.getUserSettings(userId);

        // Then
        assertTrue(result.containsKey(NotificationType.NEW_COMMUNITY_QUESTION));
        assertTrue(result.containsKey(NotificationType.NEW_COMMUNITY_ANSWER));

        Map<NotificationChannel, Boolean> questionSettings =
                result.get(NotificationType.NEW_COMMUNITY_QUESTION);
        assertTrue(questionSettings.get(NotificationChannel.IN_APP));
        assertFalse(questionSettings.get(NotificationChannel.EMAIL));
    }

    @Test
    void 신규_사용자_알림_설정_초기화() {
        // Given
        Long userId = 1L;
        User user = User.builder().id(userId).build();

        // When
        notificationSettingsService.initializeDefaultSettings(user);

        // Then
        int expectedCount = NotificationType.values().length * NotificationChannel.values().length;
        verify(settingRepository, times(expectedCount)).save(any(UserNotificationSetting.class));
    }

    @Test
    void 알림_설정_변경_성공() {
        // Given
        Long userId = 1L;
        User user = User.builder().id(userId).build();
        NotificationType type = NotificationType.NEW_COMMUNITY_QUESTION;
        NotificationChannel channel = NotificationChannel.IN_APP;

        UserNotificationSetting setting = UserNotificationSetting.createDefault(user, type, channel);

        when(settingRepository.findByUserIdAndNotificationTypeAndChannel(userId, type, channel))
                .thenReturn(Optional.of(setting));

        // When
        notificationSettingsService.updateSetting(userId, type, channel, false);

        // Then
        assertFalse(setting.isEnabled());
    }

    @Test
    void 알림_설정_변경_설정_없음_자동_생성() {
        // Given
        Long userId = 1L;
        User user = User.builder().id(userId).build();
        NotificationType type = NotificationType.NEW_COMMUNITY_QUESTION;
        NotificationChannel channel = NotificationChannel.IN_APP;

        UserNotificationSetting newSetting = UserNotificationSetting.createDefault(user, type, channel);

        when(settingRepository.findByUserIdAndNotificationTypeAndChannel(userId, type, channel))
                .thenReturn(Optional.empty());
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        when(settingRepository.save(any(UserNotificationSetting.class)))
                .thenReturn(newSetting);

        // When
        notificationSettingsService.updateSetting(userId, type, channel, false);

        // Then
        verify(settingRepository).save(any(UserNotificationSetting.class));
        assertFalse(newSetting.isEnabled());
    }

    @Test
    void 일괄_설정_변경() {
        // Given
        Long userId = 1L;
        User user = User.builder().id(userId).build();
        NotificationType type = NotificationType.NEW_COMMUNITY_QUESTION;

        UserNotificationSetting inAppSetting = UserNotificationSetting.createDefault(
                user, type, NotificationChannel.IN_APP);
        UserNotificationSetting emailSetting = UserNotificationSetting.createDefault(
                user, type, NotificationChannel.EMAIL);

        when(settingRepository.findByUserIdAndNotificationTypeAndChannel(
                userId, type, NotificationChannel.IN_APP))
                .thenReturn(Optional.of(inAppSetting));
        when(settingRepository.findByUserIdAndNotificationTypeAndChannel(
                userId, type, NotificationChannel.EMAIL))
                .thenReturn(Optional.of(emailSetting));

        // When
        notificationSettingsService.updateSettingsForType(userId, type, true, false);

        // Then
        assertTrue(inAppSetting.isEnabled());
        assertFalse(emailSetting.isEnabled());
    }

    @Test
    void 모든_알림_끄기() {
        // Given
        Long userId = 1L;
        User user = User.builder().id(userId).build();

        UserNotificationSetting setting1 = UserNotificationSetting.createDefault(
                user, NotificationType.NEW_COMMUNITY_QUESTION, NotificationChannel.IN_APP);
        setting1.updateEnabled(true);

        UserNotificationSetting setting2 = UserNotificationSetting.createDefault(
                user, NotificationType.NEW_COMMUNITY_ANSWER, NotificationChannel.EMAIL);
        setting2.updateEnabled(true);

        List<UserNotificationSetting> settings = List.of(setting1, setting2);

        when(settingRepository.findByUserId(userId)).thenReturn(settings);

        // When
        notificationSettingsService.toggleAllNotifications(userId, false);

        // Then
        assertFalse(setting1.isEnabled());
        assertFalse(setting2.isEnabled());
    }

    @Test
    void 모든_알림_켜기() {
        // Given
        Long userId = 1L;
        User user = User.builder().id(userId).build();

        UserNotificationSetting setting1 = UserNotificationSetting.createDefault(
                user, NotificationType.NEW_COMMUNITY_QUESTION, NotificationChannel.IN_APP);
        setting1.updateEnabled(false);

        UserNotificationSetting setting2 = UserNotificationSetting.createDefault(
                user, NotificationType.NEW_COMMUNITY_ANSWER, NotificationChannel.EMAIL);
        setting2.updateEnabled(false);

        List<UserNotificationSetting> settings = List.of(setting1, setting2);

        when(settingRepository.findByUserId(userId)).thenReturn(settings);

        // When
        notificationSettingsService.toggleAllNotifications(userId, true);

        // Then
        assertTrue(setting1.isEnabled());
        assertTrue(setting2.isEnabled());
    }

    @Test
    void User_생성_이벤트_리스너_호출() {
        // Given
        Long userId = 1L;
        User user = User.builder().id(userId).build();
        UserCreatedEvent event = new UserCreatedEvent(user);

        // When
        notificationSettingsService.handleUserCreatedEvent(event);

        // Then
        int expectedCount = NotificationType.values().length * NotificationChannel.values().length;
        verify(settingRepository, times(expectedCount)).save(any(UserNotificationSetting.class));
    }
}
