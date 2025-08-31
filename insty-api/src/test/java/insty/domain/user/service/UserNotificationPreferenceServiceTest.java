package insty.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;

import insty.domain.user.dto.request.UserNotificationPreferenceUpdateReq;
import insty.domain.user.dto.response.UserNotificationPreferenceRes;
import insty.ai.adapter.AiRequester;
import insty.cloudfront.adapter.CloudFrontSigner;
import insty.domain.user.repository.UserNotificationPreferenceRepository;
import insty.domain.user.repository.UserRepository;
import insty.global.property.AppProperties;
import insty.model.user.User;
import insty.model.user.UserNotificationPreference;
import insty.s3.adapter.S3FileManager;
import insty.s3.adapter.S3UrlIssuer;
import java.util.Optional;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Tag("integration")
class UserNotificationPreferenceServiceTest {

    @Autowired
    private UserNotificationPreferenceService userNotificationPreferenceService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserNotificationPreferenceRepository userNotificationPreferenceRepository;

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

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) "
                    + "VALUES (1, 'example@example.com', 'example', '1234', null, 'LEARNER', false, null, true, NOW(), NOW(), NOW());"
    })
    @Test
    void getUserNotificationPreference_설정_없음_자동_생성() {
        // given
        Long userId = 1L;

        // when
        UserNotificationPreferenceRes result = userNotificationPreferenceService.getUserNotificationPreference(userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.userMentionNotificationEnabled()).isTrue();
        assertThat(result.userMentionEmailEnabled()).isTrue();
        assertThat(result.newQuestionNotificationEnabled()).isTrue();
        assertThat(result.newQuestionEmailEnabled()).isTrue();
        assertThat(result.newAnswerNotificationEnabled()).isTrue();
        assertThat(result.newAnswerEmailEnabled()).isTrue();
        assertThat(result.answerAcceptedNotificationEnabled()).isTrue();
        assertThat(result.answerAcceptedEmailEnabled()).isTrue();

        // 데이터베이스에 생성되었는지 확인
        Optional<UserNotificationPreference> savedPreference = userNotificationPreferenceRepository.findByUserId(userId);
        assertThat(savedPreference).isPresent();
        assertThat(savedPreference.get().getUser().getId()).isEqualTo(userId);
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) "
                    + "VALUES (1, 'example@example.com', 'example', '1234', null, 'LEARNER', false, null, true, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.user_notification_preferences (id, user_id, user_mention_notification_enabled, user_mention_email_enabled, new_question_notification_enabled, new_question_email_enabled, new_answer_notification_enabled, new_answer_email_enabled, answer_accepted_notification_enabled, answer_accepted_email_enabled, created_at, updated_at) "
                    + "VALUES (1, 1, false, false, false, false, false, false, false, false, NOW(), NOW());"
    })
    @Test
    void getUserNotificationPreference_기존_설정_조회() {
        // given
        Long userId = 1L;

        // when
        UserNotificationPreferenceRes result = userNotificationPreferenceService.getUserNotificationPreference(userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.userMentionNotificationEnabled()).isFalse();
        assertThat(result.userMentionEmailEnabled()).isFalse();
        assertThat(result.newQuestionNotificationEnabled()).isFalse();
        assertThat(result.newQuestionEmailEnabled()).isFalse();
        assertThat(result.newAnswerNotificationEnabled()).isFalse();
        assertThat(result.newAnswerEmailEnabled()).isFalse();
        assertThat(result.answerAcceptedNotificationEnabled()).isFalse();
        assertThat(result.answerAcceptedEmailEnabled()).isFalse();
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) "
                    + "VALUES (1, 'example@example.com', 'example', '1234', null, 'LEARNER', false, null, true, NOW(), NOW(), NOW());"
    })
    @Test
    void updateUserMentionSettings_정상() {
        // given
        Long userId = 1L;
        boolean notificationEnabled = false;
        boolean emailEnabled = true;

        // when
        UserNotificationPreferenceRes result = userNotificationPreferenceService.updateUserMentionSettings(
                userId, notificationEnabled, emailEnabled);

        // then
        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.userMentionNotificationEnabled()).isFalse();
        assertThat(result.userMentionEmailEnabled()).isTrue();
        // 다른 설정들은 기본값 유지
        assertThat(result.newQuestionNotificationEnabled()).isTrue();
        assertThat(result.newQuestionEmailEnabled()).isTrue();

        // 데이터베이스 상태 확인
        Optional<UserNotificationPreference> savedPreference = userNotificationPreferenceRepository.findByUserId(userId);
        assertThat(savedPreference).isPresent();
        assertThat(savedPreference.get().isUserMentionNotificationEnabled()).isFalse();
        assertThat(savedPreference.get().isUserMentionEmailEnabled()).isTrue();
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) "
                    + "VALUES (1, 'example@example.com', 'example', '1234', null, 'LEARNER', false, null, true, NOW(), NOW(), NOW());"
    })
    @Test
    void updateAllSettings_정상() {
        // given
        Long userId = 1L;
        UserNotificationPreferenceUpdateReq req = new UserNotificationPreferenceUpdateReq(
                false, true,   // 멘션: 알림 비활성화, 이메일 활성화
                true, false,   // 새 질문: 알림 활성화, 이메일 비활성화
                false, false,  // 새 답변: 알림 비활성화, 이메일 비활성화
                true, true,    // 답변 채택: 알림 활성화, 이메일 활성화
                true, false    // 요청한 강의 등록: 알림 활성화, 이메일 비활성화
        );

        // when
        UserNotificationPreferenceRes result = userNotificationPreferenceService.updateAllSettings(userId, req);

        // then
        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.userMentionNotificationEnabled()).isFalse();
        assertThat(result.userMentionEmailEnabled()).isTrue();
        assertThat(result.newQuestionNotificationEnabled()).isTrue();
        assertThat(result.newQuestionEmailEnabled()).isFalse();
        assertThat(result.newAnswerNotificationEnabled()).isFalse();
        assertThat(result.newAnswerEmailEnabled()).isFalse();
        assertThat(result.answerAcceptedNotificationEnabled()).isTrue();
        assertThat(result.answerAcceptedEmailEnabled()).isTrue();
        assertThat(result.requestedCourseRegistrationNotificationEnabled()).isTrue();
        assertThat(result.requestedCourseRegistrationEmailEnabled()).isFalse();

        // 데이터베이스 상태 확인
        Optional<UserNotificationPreference> savedPreference = userNotificationPreferenceRepository.findByUserId(userId);
        assertThat(savedPreference).isPresent();
        UserNotificationPreference preference = savedPreference.get();
        assertThat(preference.isUserMentionNotificationEnabled()).isFalse();
        assertThat(preference.isUserMentionEmailEnabled()).isTrue();
        assertThat(preference.isNewQuestionNotificationEnabled()).isTrue();
        assertThat(preference.isNewQuestionEmailEnabled()).isFalse();
        assertThat(preference.isNewAnswerNotificationEnabled()).isFalse();
        assertThat(preference.isNewAnswerEmailEnabled()).isFalse();
        assertThat(preference.isAnswerAcceptedNotificationEnabled()).isTrue();
        assertThat(preference.isAnswerAcceptedEmailEnabled()).isTrue();
        assertThat(preference.isRequestedCourseRegistrationNotificationEnabled()).isTrue();
        assertThat(preference.isRequestedCourseRegistrationEmailEnabled()).isFalse();
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) "
                    + "VALUES (1, 'example@example.com', 'example', '1234', null, 'LEARNER', false, null, true, NOW(), NOW(), NOW());"
    })
    @Test
    void shouldReceiveUserMentionEmail_이메일_수신_동의_활성화() {
        // given
        Long userId = 1L;
        User user = userRepository.findById(userId).orElseThrow();

        // when
        boolean result = userNotificationPreferenceService.shouldReceiveUserMentionEmail(user);

        // then
        assertThat(result).isTrue(); // 이메일 수신 동의 + 기본 설정(이메일 알림 활성화)
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) "
                    + "VALUES (1, 'example@example.com', 'example', '1234', null, 'LEARNER', false, null, false, NOW(), NOW(), NOW());"
    })
    @Test
    void shouldReceiveUserMentionEmail_이메일_수신_동의_비활성화() {
        // given
        Long userId = 1L;
        User user = userRepository.findById(userId).orElseThrow();

        // when
        boolean result = userNotificationPreferenceService.shouldReceiveUserMentionEmail(user);

        // then
        assertThat(result).isFalse(); // 이메일 수신 비동의로 인해 false
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) "
                    + "VALUES (1, 'example@example.com', 'example', '1234', null, 'LEARNER', false, null, true, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.user_notification_preferences (id, user_id, user_mention_notification_enabled, user_mention_email_enabled, new_question_notification_enabled, new_question_email_enabled, new_answer_notification_enabled, new_answer_email_enabled, answer_accepted_notification_enabled, answer_accepted_email_enabled, requested_course_registration_notification_enabled, requested_course_registration_email_enabled, created_at, updated_at) "
                    + "VALUES (1, 1, true, false, true, true, true, true, true, true, true, true, NOW(), NOW());"
    })
    @Test
    void shouldReceiveUserMentionEmail_이메일_알림_비활성화() {
        // given
        Long userId = 1L;
        User user = userRepository.findById(userId).orElseThrow();

        // when
        boolean result = userNotificationPreferenceService.shouldReceiveUserMentionEmail(user);

        // then
        assertThat(result).isFalse(); // 이메일 알림 비활성화로 인해 false
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) "
                    + "VALUES (1, 'example@example.com', 'example', '1234', null, 'LEARNER', false, null, true, NOW(), NOW(), NOW());"
    })
    @Test
    void updateNewQuestionSettings_정상() {
        // given
        Long userId = 1L;
        boolean notificationEnabled = true;
        boolean emailEnabled = false;

        // when
        UserNotificationPreferenceRes result = userNotificationPreferenceService.updateNewQuestionSettings(
                userId, notificationEnabled, emailEnabled);

        // then
        assertThat(result).isNotNull();
        assertThat(result.newQuestionNotificationEnabled()).isTrue();
        assertThat(result.newQuestionEmailEnabled()).isFalse();

        // 데이터베이스 상태 확인
        Optional<UserNotificationPreference> savedPreference = userNotificationPreferenceRepository.findByUserId(userId);
        assertThat(savedPreference).isPresent();
        assertThat(savedPreference.get().isNewQuestionNotificationEnabled()).isTrue();
        assertThat(savedPreference.get().isNewQuestionEmailEnabled()).isFalse();
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) "
                    + "VALUES (1, 'example@example.com', 'example', '1234', null, 'LEARNER', false, null, true, NOW(), NOW(), NOW());"
    })
    @Test
    void updateNewAnswerSettings_정상() {
        // given
        Long userId = 1L;
        boolean notificationEnabled = false;
        boolean emailEnabled = true;

        // when
        UserNotificationPreferenceRes result = userNotificationPreferenceService.updateNewAnswerSettings(
                userId, notificationEnabled, emailEnabled);

        // then
        assertThat(result).isNotNull();
        assertThat(result.newAnswerNotificationEnabled()).isFalse();
        assertThat(result.newAnswerEmailEnabled()).isTrue();

        // 데이터베이스 상태 확인
        Optional<UserNotificationPreference> savedPreference = userNotificationPreferenceRepository.findByUserId(userId);
        assertThat(savedPreference).isPresent();
        assertThat(savedPreference.get().isNewAnswerNotificationEnabled()).isFalse();
        assertThat(savedPreference.get().isNewAnswerEmailEnabled()).isTrue();
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) "
                    + "VALUES (1, 'example@example.com', 'example', '1234', null, 'LEARNER', false, null, true, NOW(), NOW(), NOW());"
    })
    @Test
    void updateAnswerAcceptedSettings_정상() {
        // given
        Long userId = 1L;
        boolean notificationEnabled = true;
        boolean emailEnabled = false;

        // when
        UserNotificationPreferenceRes result = userNotificationPreferenceService.updateAnswerAcceptedSettings(
                userId, notificationEnabled, emailEnabled);

        // then
        assertThat(result).isNotNull();
        assertThat(result.answerAcceptedNotificationEnabled()).isTrue();
        assertThat(result.answerAcceptedEmailEnabled()).isFalse();

        // 데이터베이스 상태 확인
        Optional<UserNotificationPreference> savedPreference = userNotificationPreferenceRepository.findByUserId(userId);
        assertThat(savedPreference).isPresent();
        assertThat(savedPreference.get().isAnswerAcceptedNotificationEnabled()).isTrue();
        assertThat(savedPreference.get().isAnswerAcceptedEmailEnabled()).isFalse();
    }
}