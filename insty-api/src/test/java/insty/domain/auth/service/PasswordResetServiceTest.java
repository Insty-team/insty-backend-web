package insty.domain.auth.service;

import insty.ai.adapter.AiRequester;
import insty.cloudfront.adapter.CloudFrontSigner;
import insty.domain.auth.dto.response.PasswordResetVerifyRes;
import insty.domain.auth.implement.emailverification.MailAuthenticateContent;
import insty.domain.auth.implement.passwordreset.PasswordResetReader;
import insty.domain.auth.implement.passwordreset.PasswordResetWriter;
import insty.domain.user.implement.UserReader;
import insty.domain.video.repository.VideoEncodingRepository;
import insty.error.AuthErrorCode;
import insty.error.UserErrorCode;
import insty.exception.CustomException;
import insty.global.property.AppProperties;
import insty.mail.MailHelper;
import insty.model.auth.PasswordResetVerification;
import insty.model.auth.TokenGenerator;
import insty.model.user.User;
import insty.redis.adapter.RedisService;
import insty.s3.adapter.S3FileManager;
import insty.s3.adapter.S3UrlIssuer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
@Tag("integration")
@ActiveProfiles("test")
class PasswordResetServiceTest {

    @Autowired
    PasswordResetService passwordResetService;

    @Autowired
    UserReader userReader;

    @Autowired
    PasswordResetWriter passwordResetWriter;

    @Autowired
    PasswordResetReader passwordResetReader;

    @MockitoBean
    MailHelper mailHelper;

    @MockitoBean
    TokenGenerator tokenGenerator;

    @MockitoBean
    private RedisService redisService;

    @MockitoBean
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @MockitoBean
    private AppProperties appProperties;

    @MockitoBean
    private S3UrlIssuer s3UrlIssuer;
    @MockitoBean
    private S3FileManager s3FileManager;
    @MockitoBean
    private CloudFrontSigner cloudFrontSigner;
    @MockitoBean
    private AiRequester aiRequester;
    @MockitoBean
    private VideoEncodingRepository videoEncodingRepository;

    private static final String TEST_EMAIL_EXIST = "example@example.com";
    private static final String TEST_EMAIL_NOT_EXIST = "notfound@test.com";
    private static final String PASSWORD_HEADER_PREFIX = "pw-reset:";
    private static final String TEST_CODE = "abcdef";
    private static final String EXPIRED_TIME = LocalDateTime.now().plusMinutes(5).toString();
    private static final String TEST_JSON =  String.format(
            "{\"email\":\"example@example.com\",\"code\":\"abcdef\",\"verified\":false,\"expiredAt\":\"%s\"}",
            EXPIRED_TIME
    );
    private static final String TEST_JSON_VERIFIED =  String.format(
            "{\"email\":\"example@example.com\",\"code\":\"abcdef\",\"verified\":true,\"expiredAt\":\"%s\"}",
            EXPIRED_TIME
    );
    private static final String RAW_PASSWORD = "newPw123";

    @Test
    @DisplayName("비밀번호 재설정 이메일 발송 성공")
    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) "
                    + "VALUES (1, 'example@example.com', 'example', 1234, null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());"
    })
    void sendResetMail_success() {
        // given
        PasswordResetVerification verification = PasswordResetVerification.create(TEST_EMAIL_EXIST, length -> TEST_CODE);
        //mock
        when(tokenGenerator.generate(anyInt())).thenReturn(TEST_CODE);
        when(redisService.find(anyString())).thenReturn(Optional.of(TEST_JSON));

        // when
        passwordResetService.sendResetMail(TEST_EMAIL_EXIST);

        // then: 메일이 실제로 발송되는지 확인
        ArgumentCaptor<MailAuthenticateContent> captor = ArgumentCaptor.forClass(MailAuthenticateContent.class);

        verify(mailHelper, times(1)).send(captor.capture());
        verify(redisService, times(1)).save(any(),any(),any());
        MailAuthenticateContent content = captor.getValue();
        assertThat(content.to()).isEqualTo(TEST_EMAIL_EXIST);
        assertThat(content.variables().get("code")).isEqualTo(verification.getCode());

    }

    @Test
    @DisplayName("인증코드 검증 성공")
    void verifyCode_success() {
        // given
        //mock
        when(redisService.find(anyString())).thenReturn(Optional.of(TEST_JSON));
        // when
        PasswordResetVerifyRes verifyRes = passwordResetService.verifyCode(TEST_EMAIL_EXIST, TEST_CODE);

        // then
        assertThat(verifyRes.verified()).isTrue();
        verify(redisService).save(any(),any(),any());
    }

    @Test
    @DisplayName("비밀번호 변경 성공(인증 완료 상태)")
    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) "
                    + "VALUES (1, 'example@example.com', 'example', 1234, null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());"
    })
    void updatePassword_success() {
        // given
        User userByEmail = userReader.getUserByEmail(TEST_EMAIL_EXIST);
        //mock
        when(bCryptPasswordEncoder.encode(RAW_PASSWORD)).thenReturn("encodedPw");
        when(redisService.find(anyString())).thenReturn(Optional.of(TEST_JSON_VERIFIED));
        // when
        passwordResetService.updatePassword(TEST_EMAIL_EXIST, RAW_PASSWORD);

       //then
        assertThat(userByEmail.getPassword()).isEqualTo("encodedPw");
    }

    @Test
    @DisplayName("비밀번호 변경 성공후 토큰 삭제")
    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) "
                    + "VALUES (1, 'example@example.com', 'example', 1234, null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());"
    })
    void delete_token_success() {
        // given
        User userByEmail = userReader.getUserByEmail(TEST_EMAIL_EXIST);
        //mock
        when(bCryptPasswordEncoder.encode(RAW_PASSWORD)).thenReturn("encodedPw");
        when(redisService.find(anyString())).thenReturn(Optional.of(TEST_JSON_VERIFIED));
        doAnswer(invocation -> {
            // delete가 호출되면, 이후 find(redisKey) 호출은 빈 Optional을 반환하도록 재설정
            when(redisService.find(PASSWORD_HEADER_PREFIX + TEST_EMAIL_EXIST)).thenReturn(Optional.empty());
            return null;
        }).when(redisService).delete(PASSWORD_HEADER_PREFIX + TEST_EMAIL_EXIST);
        // when
        passwordResetService.updatePassword(TEST_EMAIL_EXIST, RAW_PASSWORD);

        //then
        Optional<String> afterDelete = redisService.find(PASSWORD_HEADER_PREFIX + TEST_EMAIL_EXIST);
        assertThat(afterDelete).isEmpty(); // Mock이 Empty를 반환하는지 확인
    }

    @Test
    @DisplayName("비밀번호 변경 실패(인증 안됨)")
    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) "
                    + "VALUES (1, 'example@example.com', 'example', 1234, null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());"
    })
    void updatePassword_notVerified() {
        // given
        //mock
        when(redisService.find(anyString())).thenReturn(Optional.empty());
        // when & then
        assertThatThrownBy(() -> passwordResetService.updatePassword(TEST_EMAIL_EXIST, RAW_PASSWORD))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(AuthErrorCode.EMAIL_NOT_VERIFIED);
    }
}