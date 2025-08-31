package insty.domain.user.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import insty.domain.user.repository.UserNotificationPreferenceRepository;
import insty.model.user.User;
import insty.model.user.UserFixtureBuilder;
import insty.model.user.UserNotificationPreference;
import insty.model.user.UserNotificationPreferenceFixture;
import insty.model.user.UserNotificationPreferenceFixtureBuilder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class UserNotificationPreferenceWriterTest {

    @InjectMocks
    private UserNotificationPreferenceWriter userNotificationPreferenceWriter;

    @Mock
    private UserNotificationPreferenceRepository userNotificationPreferenceRepository;

    @Test
    void createDefaultPreference_정상() {
        // given
        User user = UserFixtureBuilder.getUserWithId();
        UserNotificationPreference expectedPreference = UserNotificationPreferenceFixture.getDefaultPreference(user);

        // mock
        when(userNotificationPreferenceRepository.save(any(UserNotificationPreference.class)))
                .thenReturn(expectedPreference);

        // when
        UserNotificationPreference result = userNotificationPreferenceWriter.createDefaultPreference(user);

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
        assertThat(result.isRequestedCourseRegistrationNotificationEnabled()).isTrue();
        assertThat(result.isRequestedCourseRegistrationEmailEnabled()).isTrue();

        verify(userNotificationPreferenceRepository).save(any(UserNotificationPreference.class));
    }

    @Test
    void updateUserMentionSettings_정상() {
        // given
        User user = UserFixtureBuilder.getUserWithId();
        UserNotificationPreference preference = UserNotificationPreferenceFixtureBuilder.getPreferenceWithId(1L, user);
        boolean notificationEnabled = false;
        boolean emailEnabled = false;

        UserNotificationPreference updatedPreference = UserNotificationPreferenceFixture.getCustomPreference(
                user, false, false, true, true, true, true, true, true, true, true);

        // mock
        when(userNotificationPreferenceRepository.save(preference))
                .thenReturn(updatedPreference);

        // when
        UserNotificationPreference result = userNotificationPreferenceWriter.updateUserMentionSettings(
                preference, notificationEnabled, emailEnabled);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isUserMentionNotificationEnabled()).isFalse();
        assertThat(result.isUserMentionEmailEnabled()).isFalse();

        verify(userNotificationPreferenceRepository).save(preference);
    }

    @Test
    void updateNewQuestionSettings_정상() {
        // given
        User user = UserFixtureBuilder.getUserWithId();
        UserNotificationPreference preference = UserNotificationPreferenceFixtureBuilder.getPreferenceWithId(1L, user);
        boolean notificationEnabled = false;
        boolean emailEnabled = true;

        UserNotificationPreference updatedPreference = UserNotificationPreferenceFixture.getCustomPreference(
                user, true, true, false, true, true, true, true, true, true, true);

        // mock
        when(userNotificationPreferenceRepository.save(preference))
                .thenReturn(updatedPreference);

        // when
        UserNotificationPreference result = userNotificationPreferenceWriter.updateNewQuestionSettings(
                preference, notificationEnabled, emailEnabled);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isNewQuestionNotificationEnabled()).isFalse();
        assertThat(result.isNewQuestionEmailEnabled()).isTrue();

        verify(userNotificationPreferenceRepository).save(preference);
    }

    @Test
    void updateNewAnswerSettings_정상() {
        // given
        User user = UserFixtureBuilder.getUserWithId();
        UserNotificationPreference preference = UserNotificationPreferenceFixtureBuilder.getPreferenceWithId(1L, user);
        boolean notificationEnabled = true;
        boolean emailEnabled = false;

        UserNotificationPreference updatedPreference = UserNotificationPreferenceFixture.getCustomPreference(
                user, true, true, true, true, true, false, true, true, true, true);

        // mock
        when(userNotificationPreferenceRepository.save(preference))
                .thenReturn(updatedPreference);

        // when
        UserNotificationPreference result = userNotificationPreferenceWriter.updateNewAnswerSettings(
                preference, notificationEnabled, emailEnabled);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isNewAnswerNotificationEnabled()).isTrue();
        assertThat(result.isNewAnswerEmailEnabled()).isFalse();

        verify(userNotificationPreferenceRepository).save(preference);
    }

    @Test
    void updateAnswerAcceptedSettings_정상() {
        // given
        User user = UserFixtureBuilder.getUserWithId();
        UserNotificationPreference preference = UserNotificationPreferenceFixtureBuilder.getPreferenceWithId(1L, user);
        boolean notificationEnabled = false;
        boolean emailEnabled = false;

        UserNotificationPreference updatedPreference = UserNotificationPreferenceFixture.getCustomPreference(
                user, true, true, true, true, true, true, false, false, true, true);

        // mock
        when(userNotificationPreferenceRepository.save(preference))
                .thenReturn(updatedPreference);

        // when
        UserNotificationPreference result = userNotificationPreferenceWriter.updateAnswerAcceptedSettings(
                preference, notificationEnabled, emailEnabled);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isAnswerAcceptedNotificationEnabled()).isFalse();
        assertThat(result.isAnswerAcceptedEmailEnabled()).isFalse();

        verify(userNotificationPreferenceRepository).save(preference);
    }

    @Test
    void updateRequestedCourseRegistrationSettings_정상() {
        // given
        User user = UserFixtureBuilder.getUserWithId();
        UserNotificationPreference preference = UserNotificationPreferenceFixtureBuilder.getPreferenceWithId(1L, user);
        boolean notificationEnabled = false;
        boolean emailEnabled = true;

        UserNotificationPreference updatedPreference = UserNotificationPreferenceFixture.getCustomPreference(
                user, true, true, true, true, true, true, true, true, false, true);

        // mock
        when(userNotificationPreferenceRepository.save(preference))
                .thenReturn(updatedPreference);

        // when
        UserNotificationPreference result = userNotificationPreferenceWriter.updateRequestedCourseRegistrationSettings(
                preference, notificationEnabled, emailEnabled);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isRequestedCourseRegistrationNotificationEnabled()).isFalse();
        assertThat(result.isRequestedCourseRegistrationEmailEnabled()).isTrue();

        verify(userNotificationPreferenceRepository).save(preference);
    }
}