package insty.domain.user.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import insty.domain.user.repository.UserNotificationPreferenceRepository;
import insty.error.UserErrorCode;
import insty.exception.CustomException;
import insty.model.user.User;
import insty.model.user.UserFixtureBuilder;
import insty.model.user.UserNotificationPreference;
import insty.model.user.UserNotificationPreferenceFixture;
import insty.model.user.UserNotificationPreferenceFixtureBuilder;
import java.util.Optional;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class UserNotificationPreferenceReaderTest {

    @InjectMocks
    private UserNotificationPreferenceReader userNotificationPreferenceReader;

    @Mock
    private UserNotificationPreferenceRepository userNotificationPreferenceRepository;

    @Test
    void getOrCreateDefaultPreference_기존_설정_존재() {
        // given
        User user = UserFixtureBuilder.getUserWithId();
        UserNotificationPreference existingPreference = UserNotificationPreferenceFixtureBuilder.getPreferenceWithId(1L, user);

        // mock
        when(userNotificationPreferenceRepository.findByUser(user))
                .thenReturn(Optional.of(existingPreference));

        // when
        UserNotificationPreference result = userNotificationPreferenceReader.getOrCreateDefaultPreference(user);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(existingPreference);
        assertThat(result.getUser()).isEqualTo(user);

        verify(userNotificationPreferenceRepository).findByUser(user);
        verify(userNotificationPreferenceRepository, never()).save(any());
    }

    @Test
    void getOrCreateDefaultPreference_기존_설정_없음_새로_생성() {
        // given
        User user = UserFixtureBuilder.getUserWithId();
        UserNotificationPreference newPreference = UserNotificationPreferenceFixture.getDefaultPreference(user);

        // mock
        when(userNotificationPreferenceRepository.findByUser(user))
                .thenReturn(Optional.empty());
        when(userNotificationPreferenceRepository.save(any(UserNotificationPreference.class)))
                .thenReturn(newPreference);

        // when
        UserNotificationPreference result = userNotificationPreferenceReader.getOrCreateDefaultPreference(user);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.isUserMentionNotificationEnabled()).isTrue();
        assertThat(result.isUserMentionEmailEnabled()).isTrue();
        assertThat(result.isNewQuestionNotificationEnabled()).isTrue();
        assertThat(result.isNewQuestionEmailEnabled()).isTrue();
        assertThat(result.isNewAnswerNotificationEnabled()).isTrue();
        assertThat(result.isNewAnswerEmailEnabled()).isTrue();
        assertThat(result.isAnswerAcceptedNotificationEnabled()).isTrue();
        assertThat(result.isAnswerAcceptedEmailEnabled()).isTrue();

        verify(userNotificationPreferenceRepository).findByUser(user);
        verify(userNotificationPreferenceRepository).save(any(UserNotificationPreference.class));
    }

    @Test
    void getPreference_userId로_조회_성공() {
        // given
        Long userId = 1L;
        User user = UserFixtureBuilder.getUserWithId(userId);
        UserNotificationPreference preference = UserNotificationPreferenceFixtureBuilder.getPreferenceWithId(1L, user);

        // mock
        when(userNotificationPreferenceRepository.findByUserId(userId))
                .thenReturn(Optional.of(preference));

        // when
        UserNotificationPreference result = userNotificationPreferenceReader.getPreference(userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(preference);
        assertThat(result.getUser().getId()).isEqualTo(userId);

        verify(userNotificationPreferenceRepository).findByUserId(userId);
    }

    @Test
    void getPreference_userId로_조회_설정_없음_에러() {
        // given
        Long userId = 1L;

        // mock
        when(userNotificationPreferenceRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userNotificationPreferenceReader.getPreference(userId))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(UserErrorCode.USER_NOTIFICATION_PREFERENCE_NOT_FOUND);

        verify(userNotificationPreferenceRepository).findByUserId(userId);
    }

    @Test
    void getPreference_User로_조회_성공() {
        // given
        User user = UserFixtureBuilder.getUserWithId();
        UserNotificationPreference preference = UserNotificationPreferenceFixtureBuilder.getPreferenceWithId(1L, user);

        // mock
        when(userNotificationPreferenceRepository.findByUser(user))
                .thenReturn(Optional.of(preference));

        // when
        UserNotificationPreference result = userNotificationPreferenceReader.getPreference(user);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(preference);
        assertThat(result.getUser()).isEqualTo(user);

        verify(userNotificationPreferenceRepository).findByUser(user);
    }

    @Test
    void getPreference_User로_조회_설정_없음_에러() {
        // given
        User user = UserFixtureBuilder.getUserWithId();

        // mock
        when(userNotificationPreferenceRepository.findByUser(user))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userNotificationPreferenceReader.getPreference(user))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(UserErrorCode.USER_NOTIFICATION_PREFERENCE_NOT_FOUND);

        verify(userNotificationPreferenceRepository).findByUser(user);
    }

    @Test
    void existsByUser_존재함() {
        // given
        User user = UserFixtureBuilder.getUserWithId();

        // mock
        when(userNotificationPreferenceRepository.existsByUser(user))
                .thenReturn(true);

        // when
        boolean result = userNotificationPreferenceReader.existsByUser(user);

        // then
        assertThat(result).isTrue();

        verify(userNotificationPreferenceRepository).existsByUser(user);
    }

    @Test
    void existsByUser_존재하지_않음() {
        // given
        User user = UserFixtureBuilder.getUserWithId();

        // mock
        when(userNotificationPreferenceRepository.existsByUser(user))
                .thenReturn(false);

        // when
        boolean result = userNotificationPreferenceReader.existsByUser(user);

        // then
        assertThat(result).isFalse();

        verify(userNotificationPreferenceRepository).existsByUser(user);
    }
}