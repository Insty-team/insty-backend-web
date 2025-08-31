package insty.domain.user.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import insty.domain.user.repository.UserNotificationPreferenceRepository;
import insty.model.user.User;
import insty.model.user.UserFixtureBuilder;
import insty.model.user.UserNotificationPreference;
import insty.model.user.UserNotificationPreferenceFixture;
import java.util.Optional;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class UserNotificationPreferenceValidatorTest {

    @InjectMocks
    private UserNotificationPreferenceValidator userNotificationPreferenceValidator;

    @Mock
    private UserNotificationPreferenceRepository userNotificationPreferenceRepository;

    @Test
    void shouldReceiveUserMentionEmail_정상_모두_활성화() {
        // given
        User user = UserFixtureBuilder.getUserWithId();
        user.update(true); // 이메일 수신 동의
        UserNotificationPreference preference = UserNotificationPreferenceFixture.getDefaultPreference(user);

        // mock
        when(userNotificationPreferenceRepository.findByUser(user))
                .thenReturn(Optional.of(preference));

        // when
        boolean result = userNotificationPreferenceValidator.shouldReceiveUserMentionEmail(user);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void shouldReceiveUserMentionEmail_이메일_수신_비동의() {
        // given
        User user = UserFixtureBuilder.getUserWithId();
        user.update(false); // 이메일 수신 비동의
        UserNotificationPreference preference = UserNotificationPreferenceFixture.getDefaultPreference(user);

        // mock
        when(userNotificationPreferenceRepository.findByUser(user))
                .thenReturn(Optional.of(preference));

        // when
        boolean result = userNotificationPreferenceValidator.shouldReceiveUserMentionEmail(user);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void shouldReceiveUserMentionEmail_이메일_알림_비활성화() {
        // given
        User user = UserFixtureBuilder.getUserWithId();
        user.update(true); // 이메일 수신 동의
        UserNotificationPreference preference = UserNotificationPreferenceFixture.getCustomPreference(
                user, true, false, true, true, true, true, true, true, true, true);

        // mock
        when(userNotificationPreferenceRepository.findByUser(user))
                .thenReturn(Optional.of(preference));

        // when
        boolean result = userNotificationPreferenceValidator.shouldReceiveUserMentionEmail(user);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void shouldReceiveNewQuestionEmail_정상_모두_활성화() {
        // given
        User user = UserFixtureBuilder.getUserWithId();
        user.update(true); // 이메일 수신 동의
        UserNotificationPreference preference = UserNotificationPreferenceFixture.getDefaultPreference(user);

        // mock
        when(userNotificationPreferenceRepository.findByUser(user))
                .thenReturn(Optional.of(preference));

        // when
        boolean result = userNotificationPreferenceValidator.shouldReceiveNewQuestionEmail(user);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void shouldReceiveNewQuestionEmail_이메일_알림_비활성화() {
        // given
        User user = UserFixtureBuilder.getUserWithId();
        user.update(true); // 이메일 수신 동의
        UserNotificationPreference preference = UserNotificationPreferenceFixture.getCustomPreference(
                user, true, true, true, false, true, true, true, true, true, true);

        // mock
        when(userNotificationPreferenceRepository.findByUser(user))
                .thenReturn(Optional.of(preference));

        // when
        boolean result = userNotificationPreferenceValidator.shouldReceiveNewQuestionEmail(user);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void shouldReceiveNewAnswerEmail_정상_모두_활성화() {
        // given
        User user = UserFixtureBuilder.getUserWithId();
        user.update(true); // 이메일 수신 동의
        UserNotificationPreference preference = UserNotificationPreferenceFixture.getDefaultPreference(user);

        // mock
        when(userNotificationPreferenceRepository.findByUser(user))
                .thenReturn(Optional.of(preference));

        // when
        boolean result = userNotificationPreferenceValidator.shouldReceiveNewAnswerEmail(user);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void shouldReceiveNewAnswerEmail_이메일_알림_비활성화() {
        // given
        User user = UserFixtureBuilder.getUserWithId();
        user.update(true); // 이메일 수신 동의
        UserNotificationPreference preference = UserNotificationPreferenceFixture.getCustomPreference(
                user, true, true, true, true, true, false, true, true, true, true);

        // mock
        when(userNotificationPreferenceRepository.findByUser(user))
                .thenReturn(Optional.of(preference));

        // when
        boolean result = userNotificationPreferenceValidator.shouldReceiveNewAnswerEmail(user);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void shouldReceiveAnswerAcceptedEmail_정상_모두_활성화() {
        // given
        User user = UserFixtureBuilder.getUserWithId();
        user.update(true); // 이메일 수신 동의
        UserNotificationPreference preference = UserNotificationPreferenceFixture.getDefaultPreference(user);

        // mock
        when(userNotificationPreferenceRepository.findByUser(user))
                .thenReturn(Optional.of(preference));

        // when
        boolean result = userNotificationPreferenceValidator.shouldReceiveAnswerAcceptedEmail(user);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void shouldReceiveAnswerAcceptedEmail_이메일_알림_비활성화() {
        // given
        User user = UserFixtureBuilder.getUserWithId();
        user.update(true); // 이메일 수신 동의
        UserNotificationPreference preference = UserNotificationPreferenceFixture.getCustomPreference(
                user, true, true, true, true, true, true, true, false, true, true);

        // mock
        when(userNotificationPreferenceRepository.findByUser(user))
                .thenReturn(Optional.of(preference));

        // when
        boolean result = userNotificationPreferenceValidator.shouldReceiveAnswerAcceptedEmail(user);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void shouldReceiveUserMentionNotification_정상() {
        // given
        User user = UserFixtureBuilder.getUserWithId();
        UserNotificationPreference preference = UserNotificationPreferenceFixture.getDefaultPreference(user);

        // mock
        when(userNotificationPreferenceRepository.findByUser(user))
                .thenReturn(Optional.of(preference));

        // when
        boolean result = userNotificationPreferenceValidator.shouldReceiveUserMentionNotification(user);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void shouldReceiveUserMentionNotification_알림_비활성화() {
        // given
        User user = UserFixtureBuilder.getUserWithId();
        UserNotificationPreference preference = UserNotificationPreferenceFixture.getCustomPreference(
                user, false, true, true, true, true, true, true, true, true, true);

        // mock
        when(userNotificationPreferenceRepository.findByUser(user))
                .thenReturn(Optional.of(preference));

        // when
        boolean result = userNotificationPreferenceValidator.shouldReceiveUserMentionNotification(user);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void shouldReceiveNewQuestionNotification_정상() {
        // given
        User user = UserFixtureBuilder.getUserWithId();
        UserNotificationPreference preference = UserNotificationPreferenceFixture.getDefaultPreference(user);

        // mock
        when(userNotificationPreferenceRepository.findByUser(user))
                .thenReturn(Optional.of(preference));

        // when
        boolean result = userNotificationPreferenceValidator.shouldReceiveNewQuestionNotification(user);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void shouldReceiveNewAnswerNotification_정상() {
        // given
        User user = UserFixtureBuilder.getUserWithId();
        UserNotificationPreference preference = UserNotificationPreferenceFixture.getDefaultPreference(user);

        // mock
        when(userNotificationPreferenceRepository.findByUser(user))
                .thenReturn(Optional.of(preference));

        // when
        boolean result = userNotificationPreferenceValidator.shouldReceiveNewAnswerNotification(user);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void shouldReceiveAnswerAcceptedNotification_정상() {
        // given
        User user = UserFixtureBuilder.getUserWithId();
        UserNotificationPreference preference = UserNotificationPreferenceFixture.getDefaultPreference(user);

        // mock
        when(userNotificationPreferenceRepository.findByUser(user))
                .thenReturn(Optional.of(preference));

        // when
        boolean result = userNotificationPreferenceValidator.shouldReceiveAnswerAcceptedNotification(user);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void shouldReceiveUserMentionEmail_설정_없음_새로_생성() {
        // given
        User user = UserFixtureBuilder.getUserWithId();
        user.update(true); // 이메일 수신 동의
        UserNotificationPreference newPreference = UserNotificationPreferenceFixture.getDefaultPreference(user);

        // mock
        when(userNotificationPreferenceRepository.findByUser(user))
                .thenReturn(Optional.empty());
        when(userNotificationPreferenceRepository.save(any(UserNotificationPreference.class)))
                .thenReturn(newPreference);

        // when
        boolean result = userNotificationPreferenceValidator.shouldReceiveUserMentionEmail(user);

        // then
        assertThat(result).isTrue();
    }
}