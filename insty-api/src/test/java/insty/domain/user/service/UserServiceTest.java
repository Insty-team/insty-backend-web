package insty.domain.user.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import insty.domain.user.dto.request.UserCreateReq;
import insty.domain.user.dto.response.UserCreateRes;
import insty.domain.user.dto.response.UserDuplicateCheckRes;
import insty.domain.user.implement.UserReader;
import insty.domain.user.implement.UserValidator;
import insty.domain.user.implement.UserWriter;
import insty.error.UserErrorCode;
import insty.model.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserWriter userWriter;
    @Mock private UserValidator userValidator;
    @Mock private UserReader userReader;
    @Mock private BCryptPasswordEncoder bCryptPasswordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void 회원가입시_이메일_닉네임_중복검사를_수행하고_비밀번호를_암호화하여_저장한다() {
        // given
        UserCreateReq req = new UserCreateReq("test@example.com", "plainPassword", "nickname");
        String encodedPassword = "encodedPassword";


        User savedUser = User.create(req.email(), encodedPassword, req.nickname());
        ReflectionTestUtils.setField(savedUser, "id", 1L); // ID 수동 주입 (테스트 편의)

        when(bCryptPasswordEncoder.encode(req.password())).thenReturn(encodedPassword);
        when(userWriter.save(req.email(), encodedPassword, req.nickname())).thenReturn(savedUser);
        // when
        UserCreateRes result = userService.signup(req);
        // then
        verify(userValidator).validateDuplicateEmail(req.email());
        verify(userValidator).validateDuplicateNickname(req.nickname());
        verify(bCryptPasswordEncoder).encode(req.password());
        verify(userWriter).save(req.email(), encodedPassword, req.nickname());

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.email()).isEqualTo(req.email());
        assertThat(result.nickname()).isEqualTo(req.nickname());
        assertThat(result.userType()).isEqualTo(savedUser.getUserType());
    }

    @Test
    void 이메일이_존재하지_않으면_사용가능_응답을_반환한다() {
        // given
        String email = "unique@example.com";
        when(userReader.existCheckByEmail(email)).thenReturn(false);

        // when
        UserDuplicateCheckRes result = userService.existCheckByEmail(email);

        // then
        assertThat(result.isAvailable()).isTrue();
        assertThat(result.reason()).isEqualTo("사용 가능한 이메일입니다.");
    }

    @Test
    void 이메일이_존재하면_사용불가_응답을_반환한다() {
        // given
        String email = "duplicate@example.com";
        when(userReader.existCheckByEmail(email)).thenReturn(true);

        // when
        UserDuplicateCheckRes result = userService.existCheckByEmail(email);

        // then
        assertThat(result.isAvailable()).isFalse();
        assertThat(result.reason()).isEqualTo(UserErrorCode.USER_DUPLICATE_EMAIL.getMessage());
    }

    @Test
    void 닉네임이_존재하지_않으면_사용가능_응답을_반환한다() {
        // given
        String nickname = "uniqueNick";
        when(userReader.existCheckByNickname(nickname)).thenReturn(false);

        // when
        UserDuplicateCheckRes result = userService.existsCheckByNickname(nickname);

        // then
        assertThat(result.isAvailable()).isTrue();
        assertThat(result.reason()).isEqualTo("사용 가능한 닉네임입니다.");
    }

    @Test
    void 닉네임이_존재하면_사용불가_응답을_반환한다() {
        // given
        String nickname = "duplicatedNick";
        when(userReader.existCheckByNickname(nickname)).thenReturn(true);

        // when
        UserDuplicateCheckRes result = userService.existsCheckByNickname(nickname);

        // then
        assertThat(result.isAvailable()).isFalse();
        assertThat(result.reason()).isEqualTo(UserErrorCode.USER_DUPLICATE_NICKNAME.getMessage());
    }
}