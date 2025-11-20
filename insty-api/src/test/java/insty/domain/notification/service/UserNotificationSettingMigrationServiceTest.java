package insty.domain.notification.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import insty.domain.user.repository.UserRepository;
import insty.error.UserErrorCode;
import insty.exception.CustomException;
import insty.model.user.User;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserNotificationSettingMigrationServiceTest {

    @InjectMocks
    private UserNotificationSettingMigrationService migrationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationSettingsService preferenceService;

    @Test
    void 알림_설정이_없는_모든_사용자_초기화_성공() {
        // Given
        User user1 = User.builder().id(1L).build();
        User user2 = User.builder().id(2L).build();
        User user3 = User.builder().id(3L).build();

        List<User> allUsers = List.of(user1, user2, user3);

        when(userRepository.findAll()).thenReturn(allUsers);

        // user1, user2는 설정 없음, user3는 설정 있음
        when(preferenceService.hasUserSettings(1L)).thenReturn(false);
        when(preferenceService.hasUserSettings(2L)).thenReturn(false);
        when(preferenceService.hasUserSettings(3L)).thenReturn(true);

        // When
        int result = migrationService.migrateAllUsersWithoutSettings();

        // Then
        assertEquals(2, result);
        verify(preferenceService, times(1)).initializeDefaultSettings(user1);
        verify(preferenceService, times(1)).initializeDefaultSettings(user2);
        verify(preferenceService, times(0)).initializeDefaultSettings(user3);
    }


    @Test
    void 모든_사용자_설정_있음() {
        // Given
        User user1 = User.builder().id(1L).build();
        User user2 = User.builder().id(2L).build();

        List<User> allUsers = List.of(user1, user2);

        when(userRepository.findAll()).thenReturn(allUsers);

        // 모든 사용자가 이미 설정을 가지고 있음
        when(preferenceService.hasUserSettings(1L)).thenReturn(true);
        when(preferenceService.hasUserSettings(2L)).thenReturn(true);

        // When
        int result = migrationService.migrateAllUsersWithoutSettings();

        // Then
        assertEquals(0, result);
        verify(preferenceService, times(0)).initializeDefaultSettings(any());
    }

    @Test
    void 특정_사용자_설정_강제_초기화_성공() {
        // Given
        Long userId = 1L;
        User user = User.builder().id(userId).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        migrationService.resetUserSettings(userId);

        // Then
        verify(userRepository).findById(userId);
        verify(preferenceService).initializeDefaultSettings(user);
    }

    @Test
    void 특정_사용자_설정_강제_초기화_실패_사용자_없음() {
        // Given
        Long userId = 999L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        CustomException exception = assertThrows(CustomException.class,
                () -> migrationService.resetUserSettings(userId));

        assertEquals(UserErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        verify(preferenceService, times(0)).initializeDefaultSettings(any());
    }
}
