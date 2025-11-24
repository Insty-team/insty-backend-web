package insty.domain.notification.service;

import static org.assertj.core.api.Assertions.assertThat;

import insty.ai.adapter.AiRequester;
import insty.cloudfront.adapter.CloudFrontSigner;
import insty.domain.notification.repository.UserNotificationSettingRepository;
import insty.domain.user.dto.request.UserAgreementUpdateReq;
import insty.domain.user.event.UserCreatedEvent;
import insty.domain.user.repository.UserRepository;
import insty.domain.user.service.UserService;
import insty.global.property.AppProperties;
import insty.model.notification.UserNotificationSetting;
import insty.model.user.User;
import insty.notification.NotificationChannel;
import insty.notification.NotificationType;
import insty.s3.adapter.S3FileManager;
import insty.s3.adapter.S3UrlIssuer;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Tag("integration")
class NotificationSettingsServiceIntTest {

    @Autowired
    private NotificationSettingsService notificationSettingsService;

    @Autowired
    private UserNotificationSettingRepository settingRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private S3UrlIssuer s3UrlIssuer;

    @MockitoBean
    private S3FileManager s3FileManager;

    @MockitoBean
    private CloudFrontSigner cloudFrontSigner;

    @MockitoBean
    private AiRequester aiRequester;

    @MockitoBean
    private AppProperties appProperties;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.create(
                "test@example.com",
                "password123",
                "테스트유저"
        );
        userRepository.save(testUser);
    }

    @Test
    void 알림_수신_허용_확인_설정_있음_활성화() {
        // Given
        notificationSettingsService.initializeDefaultSettings(testUser);

        UserNotificationSetting setting = settingRepository.findByUserIdAndNotificationTypeAndChannel(
                testUser.getId(),
                NotificationType.NEW_COMMUNITY_QUESTION,
                NotificationChannel.IN_APP
        ).orElseThrow();

        setting.updateEnabled(true);

        // When
        boolean result = notificationSettingsService.isNotificationEnabled(
                testUser.getId(),
                NotificationType.NEW_COMMUNITY_QUESTION,
                NotificationChannel.IN_APP
        );

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void 알림_수신_허용_확인_설정_있음_비활성화() {
        // Given
        notificationSettingsService.initializeDefaultSettings(testUser);

        UserNotificationSetting setting = settingRepository.findByUserIdAndNotificationTypeAndChannel(
                testUser.getId(),
                NotificationType.NEW_COMMUNITY_QUESTION,
                NotificationChannel.IN_APP
        ).orElseThrow();

        setting.updateEnabled(false);

        // When
        boolean result = notificationSettingsService.isNotificationEnabled(
                testUser.getId(),
                NotificationType.NEW_COMMUNITY_QUESTION,
                NotificationChannel.IN_APP
        );

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void 알림_수신_허용_확인_설정_없음_기본값_true() {
        // When
        boolean result = notificationSettingsService.isNotificationEnabled(
                testUser.getId(),
                NotificationType.NEW_COMMUNITY_QUESTION,
                NotificationChannel.IN_APP
        );

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void 이메일_수신_허용_확인_이메일_동의_안함() {
        // Given
        User user = User.builder()
                .email("no-email@example.com")
                .password("password")
                .nickname("이메일거부유저")
                .isEmailAgreed(false)
                .build();
        userRepository.save(user);

        // When
        boolean result = notificationSettingsService.isEmailEnabled(
                user,
                NotificationType.NEW_COMMUNITY_QUESTION
        );

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void 이메일_수신_허용_확인_이메일_동의함() {
        // Given
        userService.updateAgreement(testUser.getId(), new UserAgreementUpdateReq(true));
        notificationSettingsService.initializeDefaultSettings(testUser);

        // When
        boolean result = notificationSettingsService.isEmailEnabled(
                testUser,
                NotificationType.NEW_COMMUNITY_QUESTION
        );

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void 사용자_모든_알림_설정_조회() {
        // Given
        notificationSettingsService.initializeDefaultSettings(testUser);

        // When
        Map<NotificationType, Map<NotificationChannel, Boolean>> result =
                notificationSettingsService.getUserSettings(testUser.getId());

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).containsKeys(NotificationType.getUserConfigurableTypes());

        for (NotificationType type : NotificationType.getUserConfigurableTypes()) {
            assertThat(result.get(type)).containsKeys(NotificationChannel.values());
        }
    }

    @Test
    void 신규_사용자_알림_설정_초기화() {
        // When
        notificationSettingsService.initializeDefaultSettings(testUser);

        // Then
        List<UserNotificationSetting> settings = settingRepository.findByUserId(testUser.getId());

        int expectedCount = NotificationType.values().length * NotificationChannel.values().length;
        assertThat(settings).hasSize(expectedCount);

        // 기본값으로 모두 활성화되어 있는지 확인
        assertThat(settings).allMatch(UserNotificationSetting::isEnabled);
    }

    @Test
    void 알림_설정_변경_성공() {
        // Given
        notificationSettingsService.initializeDefaultSettings(testUser);

        // When
        notificationSettingsService.updateSetting(
                testUser.getId(),
                NotificationType.NEW_COMMUNITY_QUESTION,
                NotificationChannel.IN_APP,
                false
        );

        // Then
        UserNotificationSetting setting = settingRepository.findByUserIdAndNotificationTypeAndChannel(
                testUser.getId(),
                NotificationType.NEW_COMMUNITY_QUESTION,
                NotificationChannel.IN_APP
        ).orElseThrow();

        assertThat(setting.isEnabled()).isFalse();
    }

    @Test
    void 알림_설정_변경_설정_없음_자동_생성() {
        // When
        notificationSettingsService.updateSetting(
                testUser.getId(),
                NotificationType.NEW_COMMUNITY_QUESTION,
                NotificationChannel.IN_APP,
                false
        );

        // Then
        UserNotificationSetting setting = settingRepository.findByUserIdAndNotificationTypeAndChannel(
                testUser.getId(),
                NotificationType.NEW_COMMUNITY_QUESTION,
                NotificationChannel.IN_APP
        ).orElseThrow();

        assertThat(setting.isEnabled()).isFalse();
    }

    @Test
    void 일괄_설정_변경() {
        // Given
        notificationSettingsService.initializeDefaultSettings(testUser);

        // When
        notificationSettingsService.updateSettingsForType(
                testUser.getId(),
                NotificationType.NEW_COMMUNITY_QUESTION,
                true,
                false
        );

        // Then
        UserNotificationSetting inAppSetting = settingRepository.findByUserIdAndNotificationTypeAndChannel(
                testUser.getId(),
                NotificationType.NEW_COMMUNITY_QUESTION,
                NotificationChannel.IN_APP
        ).orElseThrow();

        UserNotificationSetting emailSetting = settingRepository.findByUserIdAndNotificationTypeAndChannel(
                testUser.getId(),
                NotificationType.NEW_COMMUNITY_QUESTION,
                NotificationChannel.EMAIL
        ).orElseThrow();

        assertThat(inAppSetting.isEnabled()).isTrue();
        assertThat(emailSetting.isEnabled()).isFalse();
    }

    @Test
    void 모든_알림_끄기() {
        // Given
        notificationSettingsService.initializeDefaultSettings(testUser);

        // When
        notificationSettingsService.toggleAllNotifications(testUser.getId(), false);

        // Then
        List<UserNotificationSetting> settings = settingRepository.findByUserId(testUser.getId());
        assertThat(settings).allMatch(setting -> !setting.isEnabled());
    }

    @Test
    void 모든_알림_켜기() {
        // Given
        notificationSettingsService.initializeDefaultSettings(testUser);
        notificationSettingsService.toggleAllNotifications(testUser.getId(), false);

        // When
        notificationSettingsService.toggleAllNotifications(testUser.getId(), true);

        // Then
        List<UserNotificationSetting> settings = settingRepository.findByUserId(testUser.getId());
        assertThat(settings).allMatch(UserNotificationSetting::isEnabled);
    }

    @Test
    void User_생성_이벤트_리스너_호출() {
        // Given
        User newUser = User.create(
                "new@example.com",
                "password123",
                "신규유저"
        );
        userRepository.save(newUser);

        UserCreatedEvent event = new UserCreatedEvent(newUser);

        // When
        notificationSettingsService.handleUserCreatedEvent(event);

        // Then
        List<UserNotificationSetting> settings = settingRepository.findByUserId(newUser.getId());

        int expectedCount = NotificationType.values().length * NotificationChannel.values().length;
        assertThat(settings).hasSize(expectedCount);
    }

    @Test
    void 특정_타입_설정만_변경() {
        // Given
        notificationSettingsService.initializeDefaultSettings(testUser);

        // When
        notificationSettingsService.updateSetting(
                testUser.getId(),
                NotificationType.NEW_COMMUNITY_QUESTION,
                NotificationChannel.EMAIL,
                false
        );

        // Then
        boolean emailDisabled = notificationSettingsService.isNotificationEnabled(
                testUser.getId(),
                NotificationType.NEW_COMMUNITY_QUESTION,
                NotificationChannel.EMAIL
        );

        boolean inAppEnabled = notificationSettingsService.isNotificationEnabled(
                testUser.getId(),
                NotificationType.NEW_COMMUNITY_QUESTION,
                NotificationChannel.IN_APP
        );

        boolean otherTypeEnabled = notificationSettingsService.isNotificationEnabled(
                testUser.getId(),
                NotificationType.NEW_COMMUNITY_ANSWER,
                NotificationChannel.EMAIL
        );

        assertThat(emailDisabled).isFalse();
        assertThat(inAppEnabled).isTrue();
        assertThat(otherTypeEnabled).isTrue();
    }
}
