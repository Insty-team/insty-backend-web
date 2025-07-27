package insty.domain.user.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import insty.domain.auth.implement.emailverification.EmailVerificationReader;
import insty.domain.user.dto.request.UserAgreementUpdateReq;
import insty.domain.user.dto.request.UserCreateReq;
import insty.domain.user.dto.request.UserPasswordUpdateReq;
import insty.domain.user.dto.request.UserTypeUpdateReq;
import insty.domain.user.dto.request.UserUpdateReq;
import insty.domain.user.dto.response.UserCreateRes;
import insty.domain.user.dto.response.UserDetailRes;
import insty.domain.user.implement.UserFileReader;
import insty.domain.user.implement.UserFileWriter;
import insty.domain.user.implement.UserReader;
import insty.domain.user.implement.UserValidator;
import insty.domain.user.implement.UserWriter;
import insty.error.AuthErrorCode;
import insty.exception.CustomException;
import insty.model.user.User;
import insty.model.user.UserFixtureBuilder;
import insty.model.user.UserType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserWriter userWriter;
    @Mock
    private UserValidator userValidator;
    @Mock
    private UserReader userReader;
    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Mock
    private UserFileWriter userFileWriter;
    @Mock
    private UserFileReader userFileReader;
    @Mock
    private EmailVerificationReader emailVerificationReader;

    @InjectMocks
    private UserService userService;


    @Test
    void 회원가입시_이메일_닉네임_중복검사를_수행하고_비밀번호를_암호화하여_저장한다() {
        // given
        UserCreateReq req = new UserCreateReq("test@example.com", "plainPassword", "nickname");
        String encodedPassword = "encodedPassword";

        User savedUser = UserFixtureBuilder.getUserWithId(1L, req.email(), encodedPassword, req.nickname());

        when(bCryptPasswordEncoder.encode(req.password())).thenReturn(encodedPassword);
        when(userWriter.save(req.email(), encodedPassword, req.nickname())).thenReturn(savedUser);
        when(emailVerificationReader.existsByEmail(req.email())).thenReturn(true);
        
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
    void 회원가입시_인증하지_않은_이메일이라면_예외가_발생한다() {
        // given
        UserCreateReq req = new UserCreateReq("test@example.com", "plainPassword", "nickname");
        when(emailVerificationReader.existsByEmail(any())).thenReturn(false);
        
        // when & then
        assertThatThrownBy(() -> userService.signup(req))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.REQUIRES_EMAIL_VERIFICATION_REQUEST);
    }

    @Test
    void 사용자_상세정보_조회에_성공한다() {
        // given
        Long userId = 1L;
        User mockUser = UserFixtureBuilder.getUserWithId(userId);
        String imageUrl = "https://cdn.com/profile.png";

        when(userReader.getUser(userId)).thenReturn(mockUser);
        when(userFileReader.getProfileImageUrl(mockUser)).thenReturn(imageUrl);

        // when
        UserDetailRes result = userService.getDetailUser(userId);

        // then
        verify(userReader).getUser(userId);
        verify(userFileReader).getProfileImageUrl(mockUser);
        assertThat(result).usingRecursiveComparison().isEqualTo(UserDetailRes.from(mockUser, imageUrl));
    }

    @Test
    void 사용자정보수정_비밀번호변경포함_성공한다() {
        // given
        Long userId = 1L;
        UserUpdateReq req = new UserUpdateReq(
                "new@example.com",
                "newnick",
                "소개글",
                "currentPassword!",
                "newPassword1!"
        );

        User findUser = UserFixtureBuilder.getUserWithId(userId, "old@example.com", "encodedCurrentPassword", "oldnick");
        User updatedUser = UserFixtureBuilder.getUserWithId(userId, req.email(), "encodedNewPassword", req.nickname());

        when(userReader.getUser(userId)).thenReturn(findUser);
        doNothing().when(userValidator).validateIdentityByPassword(findUser.getPassword(), req.currentPassword());
        doNothing().when(userValidator).validateDuplicateEmailExcludingSelf(userId, req.email());
        doNothing().when(userValidator).validateDuplicateNicknameExcludingSelf(userId, req.nickname());
        doNothing().when(userValidator).validatePasswordChangeAvailable(findUser.getSocialId());
        doNothing().when(userValidator).validateMatchesCurrentPassword(findUser.getPassword(), req.currentPassword(), req.newPassword());

        when(bCryptPasswordEncoder.encode(req.newPassword())).thenReturn("encodedNewPassword");
        when(userWriter.updateUser(findUser, req.email(), req.nickname(), req.introduce())).thenReturn(updatedUser);
        when(userFileReader.getProfileImageUrl(updatedUser)).thenReturn("https://profile.img/default.png");

        // when
        UserDetailRes result = userService.updateUser(userId, req, null);

        // then
        verify(userValidator).validateIdentityByPassword(findUser.getPassword(), req.currentPassword());
        verify(userValidator).validateDuplicateEmailExcludingSelf(userId, req.email());
        verify(userValidator).validateDuplicateNicknameExcludingSelf(userId, req.nickname());
        verify(userValidator).validatePasswordChangeAvailable(findUser.getSocialId());
        verify(userValidator).validateMatchesCurrentPassword(findUser.getPassword(), req.currentPassword(), req.newPassword());
        verify(userWriter).changePassword(findUser, "encodedNewPassword");
        verify(userWriter).updateUser(findUser, req.email(), req.nickname(), req.introduce());

        assertThat(result.email()).isEqualTo(req.email());
        assertThat(result.nickname()).isEqualTo(req.nickname());
    }

    @Test
    void 사용자정보수정_비밀번호변경없이_성공한다() {
        // given
        Long userId = 1L;
        UserUpdateReq req = new UserUpdateReq(
                "new@example.com",
                "newnick",
                "소개글",
                "currentPassword!",
                null
        );

        User findUser = UserFixtureBuilder.getUserWithId(userId, "old@example.com", "encodedCurrentPassword", "oldnick");
        User updatedUser = UserFixtureBuilder.getUserWithId(userId, req.email(), "encodedCurrentPassword", req.nickname());

        when(userReader.getUser(userId)).thenReturn(findUser);
        doNothing().when(userValidator).validateIdentityByPassword(findUser.getPassword(), req.currentPassword());
        doNothing().when(userValidator).validateDuplicateEmailExcludingSelf(userId, req.email());
        doNothing().when(userValidator).validateDuplicateNicknameExcludingSelf(userId, req.nickname());

        when(userWriter.updateUser(findUser, req.email(), req.nickname(), req.introduce())).thenReturn(updatedUser);
        when(userFileReader.getProfileImageUrl(updatedUser)).thenReturn("https://profile.img/default.png");

        // when
        UserDetailRes result = userService.updateUser(userId, req, null);

        // then
        verify(userValidator).validateIdentityByPassword(findUser.getPassword(), req.currentPassword());
        verify(userValidator).validateDuplicateEmailExcludingSelf(userId, req.email());
        verify(userValidator).validateDuplicateNicknameExcludingSelf(userId, req.nickname());
        verify(userValidator, never()).validatePasswordChangeAvailable(any());
        verify(userValidator, never()).validateMatchesCurrentPassword(any(), any(), any());
        verify(userWriter).updateUser(findUser, req.email(), req.nickname(), req.introduce());

        assertThat(result.email()).isEqualTo(req.email());
        assertThat(result.nickname()).isEqualTo(req.nickname());
    }

    @Test
    void 사용자_비밀번호_수정_시_암호화하여_저장한다() {
        // given
        Long userId = 1L;
        String currentPassword = "Current123!";
        String newPassword = "NewPassword123!";
        String encodedPassword = "EncodedNewPassword!";
        String profileImageUrl = "https://cdn.com/profile.png";

        UserPasswordUpdateReq req = new UserPasswordUpdateReq(currentPassword, newPassword);

        User findUser = UserFixtureBuilder.getUserWithId(userId, "user@example.com", "encodedCurrentPassword", "nickname");

        when(userReader.getUser(userId)).thenReturn(findUser);
        doNothing().when(userValidator).validateMatchesCurrentPassword(
                findUser.getPassword(), currentPassword, newPassword);
        when(bCryptPasswordEncoder.encode(newPassword)).thenReturn(encodedPassword);

        User updatedUser = UserFixtureBuilder.getUserWithId(userId, "user@example.com", encodedPassword, "nickname");

        when(userWriter.changePassword(findUser, encodedPassword)).thenReturn(updatedUser);
        when(userFileReader.getProfileImageUrl(updatedUser)).thenReturn(profileImageUrl);

        // when
        UserDetailRes result = userService.updatePassword(userId, req);

        // then
        verify(userReader).getUser(userId);
        verify(userValidator).validateMatchesCurrentPassword(
                findUser.getPassword(), currentPassword, newPassword);
        verify(bCryptPasswordEncoder).encode(newPassword);
        verify(userWriter).changePassword(findUser, encodedPassword);
        verify(userFileReader).getProfileImageUrl(updatedUser);

        assertThat(result).usingRecursiveComparison()
                .isEqualTo(UserDetailRes.from(updatedUser, profileImageUrl));
    }


    @Test
    void 사용자_타입_변경_시_업데이트가_정상적으로_동작한다() {
        // given
        Long userId = 1L;
        UserTypeUpdateReq req = new UserTypeUpdateReq(UserType.LEARNER);
        User findUser = UserFixtureBuilder.getUserWithId(userId, "user@example.com", "encodedCurrentPassword", "nickname");
        User updatedUser = UserFixtureBuilder.getUserWithId(userId); // 혹은 다른 상태
        String imageUrl = "https://cdn.com/profile.png";

        // 👉 누락된 Stub 추가
        when(userReader.getUser(userId)).thenReturn(findUser);
        when(userWriter.changeUserType(findUser, req.userType())).thenReturn(updatedUser);
        when(userFileReader.getProfileImageUrl(updatedUser)).thenReturn(imageUrl);

        // when
        UserDetailRes result = userService.updateUserType(userId, req);

        // then
        verify(userReader).getUser(userId);
        verify(userWriter).changeUserType(findUser, req.userType());
        verify(userFileReader).getProfileImageUrl(updatedUser);
        assertThat(result).usingRecursiveComparison().isEqualTo(UserDetailRes.from(updatedUser, imageUrl));
    }

    @Test
    void 사용자_수신_및_약관_동의_변경이_정상적으로_동작한다() {
        // given
        Long userId = 1L;
        UserAgreementUpdateReq req = new UserAgreementUpdateReq(true);
        User updatedUser = UserFixtureBuilder.getUserWithId(userId);
        String imageUrl = "https://cdn.com/profile.png";

        when(userReader.getUser(userId)).thenReturn(updatedUser);
        when(userWriter.changeEmailAgreementStatus(updatedUser, req.isEmailAgree())).thenReturn(updatedUser);
        when(userFileReader.getProfileImageUrl(updatedUser)).thenReturn(imageUrl);

        // when
        UserDetailRes result = userService.updateAgreement(userId, req);

        // then
        verify(userReader).getUser(userId);
        verify(userWriter).changeEmailAgreementStatus(updatedUser, req.isEmailAgree());
        verify(userFileReader).getProfileImageUrl(updatedUser);
        assertThat(result).usingRecursiveComparison().isEqualTo(UserDetailRes.from(updatedUser, imageUrl));
    }
}