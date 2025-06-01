package insty.domain.user.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import insty.domain.user.dto.CurrentUserDto;
import insty.domain.user.dto.request.UserAgreementUpdateReq;
import insty.domain.user.dto.request.UserCreateReq;
import insty.domain.user.dto.request.UserEmailCheckReq;
import insty.domain.user.dto.request.UserNicknameCheckReq;
import insty.domain.user.dto.request.UserTypeUpdateReq;
import insty.domain.user.dto.request.UserUpdateReq;
import insty.domain.user.dto.response.UserCreateRes;
import insty.domain.user.dto.response.UserDetailRes;
import insty.domain.user.dto.response.UserDuplicateCheckRes;
import insty.domain.user.implement.UserReader;
import insty.domain.user.implement.UserValidator;
import insty.domain.user.implement.UserWriter;
import insty.error.UserErrorCode;
import insty.model.user.User;
import insty.model.user.UserType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

@Tag("unit")
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
        UserEmailCheckReq req = new UserEmailCheckReq(email);
        when(userReader.existCheckByEmail(email)).thenReturn(false);

        // when
        UserDuplicateCheckRes result = userService.existCheckByEmail(req);

        // then
        assertThat(result.isAvailable()).isTrue();
        assertThat(result.reason()).isEqualTo("사용 가능한 이메일입니다.");
    }

    @Test
    void 이메일이_존재하면_사용불가_응답을_반환한다() {
        // given
        String email = "duplicate@example.com";
        UserEmailCheckReq req = new UserEmailCheckReq(email);
        when(userReader.existCheckByEmail(email)).thenReturn(true);

        // when
        UserDuplicateCheckRes result = userService.existCheckByEmail(req);

        // then
        assertThat(result.isAvailable()).isFalse();
        assertThat(result.reason()).isEqualTo(UserErrorCode.USER_DUPLICATE_EMAIL.getMessage());
    }

    @Test
    void 닉네임이_존재하지_않으면_사용가능_응답을_반환한다() {
        // given
        String nickname = "uniqueNick";
        UserNicknameCheckReq req = new UserNicknameCheckReq(nickname);
        when(userReader.existCheckByNickname(nickname)).thenReturn(false);

        // when
        UserDuplicateCheckRes result = userService.existsCheckByNickname(req);

        // then
        assertThat(result.isAvailable()).isTrue();
        assertThat(result.reason()).isEqualTo("사용 가능한 닉네임입니다.");
    }

    @Test
    void 닉네임이_존재하면_사용불가_응답을_반환한다() {
        // given
        String nickname = "duplicatedNick";
        UserNicknameCheckReq req = new UserNicknameCheckReq(nickname);
        when(userReader.existCheckByNickname(nickname)).thenReturn(true);

        // when
        UserDuplicateCheckRes result = userService.existsCheckByNickname(req);

        // then
        assertThat(result.isAvailable()).isFalse();
        assertThat(result.reason()).isEqualTo(UserErrorCode.USER_DUPLICATE_NICKNAME.getMessage());
    }

    @Test
    void 사용자_상세정보_조회에_성공한다() {
        // given
        Long userId = 1L;
        User mockUser = User.create("email@example.com", "encodedPassword", "nickname");
        CurrentUserDto currentUser = new CurrentUserDto(userId,  "email@example.com", "nickname");

        when(userReader.getUser(userId)).thenReturn(mockUser);

        // when
        UserDetailRes result = userService.getDetailUser(currentUser);

        // then
        verify(userReader).getUser(userId);
        assertThat(result).usingRecursiveComparison().isEqualTo(UserDetailRes.from(mockUser));
    }

    @Test
    void 사용자_정보_수정_시_비밀번호를_암호화하고_업데이트한다() {
        // given
        Long userId = 1L;
        UserUpdateReq req = new UserUpdateReq("new@example.com", "rawPassword1!", "newnick", "introduce");
        MultipartFile profileImage = mock(MultipartFile.class);
        String encodedPassword = "encodedPassword";

        User updatedUser = User.create("old@example.com", "mockUser", "oldnick");

        when(bCryptPasswordEncoder.encode(req.password())).thenReturn(encodedPassword);
        when(userWriter.updateUser(
                userId,
                req.email(),
                encodedPassword,
                req.nickname(),
                req.introduce(),
                profileImage
        )).thenReturn(updatedUser);

        // when
        UserDetailRes result = userService.updateUser(userId, req, profileImage);

        // then
        verify(bCryptPasswordEncoder).encode(req.password());
        verify(userWriter).updateUser(userId, req.email(), encodedPassword, req.nickname(), req.introduce(), profileImage);
        assertThat(result).usingRecursiveComparison().isEqualTo(UserDetailRes.from(updatedUser));
    }

    @Test
    void 사용자_타입_변경_시_업데이트가_정상적으로_동작한다() {
        // given
        Long userId = 1L;
        UserTypeUpdateReq req = new UserTypeUpdateReq(UserType.LEARNER);
        User updatedUser = User.create("old@example.com", "mockUser", "oldnick");

        when(userWriter.updateUserByUserType(userId, req.userType())).thenReturn(updatedUser);

        // when
        UserDetailRes result = userService.updateUserType(userId, req);

        // then
        verify(userWriter).updateUserByUserType(userId, req.userType());
        assertThat(result).usingRecursiveComparison().isEqualTo(UserDetailRes.from(updatedUser));
    }

    @Test
    void 사용자_수신_및_약관_동의_변경이_정상적으로_동작한다() {
        // given
        Long userId = 1L;
        UserAgreementUpdateReq req = new UserAgreementUpdateReq(true);
        User updatedUser = User.create("old@example.com", "mockUser", "oldnick");

        when(userWriter.updateUserByAgreement(userId, req.isEmailAgree())).thenReturn(updatedUser);

        // when
        UserDetailRes result = userService.updateAgreement(userId, req);

        // then
        verify(userWriter).updateUserByAgreement(userId, req.isEmailAgree());
        assertThat(result).usingRecursiveComparison().isEqualTo(UserDetailRes.from(updatedUser));
    }
}