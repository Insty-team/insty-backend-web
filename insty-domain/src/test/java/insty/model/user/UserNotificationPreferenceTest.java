package insty.model.user;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

@Tag("unit")
@DisplayName("UserNotificationPreference 도메인 테스트")
class UserNotificationPreferenceTest {

    @Test
    @DisplayName("기본 알림 설정으로 UserNotificationPreference를 생성한다")
    void createDefault() {
        // given
        User user = UserFixture.getUser();

        // when
        UserNotificationPreference preference = UserNotificationPreference.createDefault(user);

        // then
        assertThat(preference.getUser()).isEqualTo(user);
        assertThat(preference.isUserMentionNotificationEnabled()).isTrue();
        assertThat(preference.isNewQuestionNotificationEnabled()).isTrue();
        assertThat(preference.isNewAnswerNotificationEnabled()).isTrue();
        assertThat(preference.isAnswerAcceptedNotificationEnabled()).isTrue();
        assertThat(preference.isUserMentionEmailEnabled()).isTrue();
        assertThat(preference.isNewQuestionEmailEnabled()).isTrue();
        assertThat(preference.isNewAnswerEmailEnabled()).isTrue();
        assertThat(preference.isAnswerAcceptedEmailEnabled()).isTrue();
    }

    @Test
    @DisplayName("사용자 멘션 알림 설정을 업데이트한다")
    void updateUserMentionSettings() {
        // given
        User user = UserFixture.getUser();
        UserNotificationPreference preference = UserNotificationPreference.createDefault(user);

        // when
        preference.updateUserMentionSettings(false, false);

        // then
        assertThat(preference.isUserMentionNotificationEnabled()).isFalse();
        assertThat(preference.isUserMentionEmailEnabled()).isFalse();
    }

    @Test
    @DisplayName("새 질문 알림 설정을 업데이트한다")
    void updateNewQuestionSettings() {
        // given
        User user = UserFixture.getUser();
        UserNotificationPreference preference = UserNotificationPreference.createDefault(user);

        // when
        preference.updateNewQuestionSettings(false, true);

        // then
        assertThat(preference.isNewQuestionNotificationEnabled()).isFalse();
        assertThat(preference.isNewQuestionEmailEnabled()).isTrue();
    }

    @Test
    @DisplayName("새 답변 알림 설정을 업데이트한다")
    void updateNewAnswerSettings() {
        // given
        User user = UserFixture.getUser();
        UserNotificationPreference preference = UserNotificationPreference.createDefault(user);

        // when
        preference.updateNewAnswerSettings(true, false);

        // then
        assertThat(preference.isNewAnswerNotificationEnabled()).isTrue();
        assertThat(preference.isNewAnswerEmailEnabled()).isFalse();
    }

    @Test
    @DisplayName("답변 채택 알림 설정을 업데이트한다")
    void updateAnswerAcceptedSettings() {
        // given
        User user = UserFixture.getUser();
        UserNotificationPreference preference = UserNotificationPreference.createDefault(user);

        // when
        preference.updateAnswerAcceptedSettings(false, false);

        // then
        assertThat(preference.isAnswerAcceptedNotificationEnabled()).isFalse();
        assertThat(preference.isAnswerAcceptedEmailEnabled()).isFalse();
    }

    @Test
    @DisplayName("사용자 이메일 동의 여부와 알림 설정에 따라 멘션 이메일 수신 여부를 판단한다 - 모두 활성화")
    void shouldReceiveUserMentionEmail_AllEnabled() {
        // given
        User user = UserFixture.getUser();
        user.update(true); // 이메일 수신 동의
        UserNotificationPreference preference = UserNotificationPreference.createDefault(user);

        // when & then
        assertThat(preference.shouldReceiveUserMentionEmail()).isTrue();
    }

    @Test
    @DisplayName("사용자 이메일 동의 여부와 알림 설정에 따라 멘션 이메일 수신 여부를 판단한다 - 이메일 동의 비활성화")
    void shouldReceiveUserMentionEmail_EmailAgreementDisabled() {
        // given
        User user = UserFixture.getUser();
        user.update(false); // 이메일 수신 비동의
        UserNotificationPreference preference = UserNotificationPreference.createDefault(user);

        // when & then
        assertThat(preference.shouldReceiveUserMentionEmail()).isFalse();
    }

    @Test
    @DisplayName("사용자 이메일 동의 여부와 알림 설정에 따라 멘션 이메일 수신 여부를 판단한다 - 이메일 알림 비활성화")
    void shouldReceiveUserMentionEmail_EmailNotificationDisabled() {
        // given
        User user = UserFixture.getUser();
        user.update(true); // 이메일 수신 동의
        UserNotificationPreference preference = UserNotificationPreference.createDefault(user);
        preference.updateUserMentionSettings(true, false); // 이메일 알림 비활성화

        // when & then
        assertThat(preference.shouldReceiveUserMentionEmail()).isFalse();
    }

    @Test
    @DisplayName("새 질문 이메일 수신 여부를 올바르게 판단한다")
    void shouldReceiveNewQuestionEmail() {
        // given
        User user = UserFixture.getUser();
        user.update(true);
        UserNotificationPreference preference = UserNotificationPreference.createDefault(user);

        // when & then
        assertThat(preference.shouldReceiveNewQuestionEmail()).isTrue();

        // 이메일 알림만 비활성화
        preference.updateNewQuestionSettings(true, false);
        assertThat(preference.shouldReceiveNewQuestionEmail()).isFalse();
    }

    @Test
    @DisplayName("새 답변 이메일 수신 여부를 올바르게 판단한다")
    void shouldReceiveNewAnswerEmail() {
        // given
        User user = UserFixture.getUser();
        user.update(true);
        UserNotificationPreference preference = UserNotificationPreference.createDefault(user);

        // when & then
        assertThat(preference.shouldReceiveNewAnswerEmail()).isTrue();

        // 이메일 알림만 비활성화
        preference.updateNewAnswerSettings(true, false);
        assertThat(preference.shouldReceiveNewAnswerEmail()).isFalse();
    }

    @Test
    @DisplayName("답변 채택 이메일 수신 여부를 올바르게 판단한다")
    void shouldReceiveAnswerAcceptedEmail() {
        // given
        User user = UserFixture.getUser();
        user.update(true);
        UserNotificationPreference preference = UserNotificationPreference.createDefault(user);

        // when & then
        assertThat(preference.shouldReceiveAnswerAcceptedEmail()).isTrue();

        // 이메일 알림만 비활성화
        preference.updateAnswerAcceptedSettings(true, false);
        assertThat(preference.shouldReceiveAnswerAcceptedEmail()).isFalse();
    }
}