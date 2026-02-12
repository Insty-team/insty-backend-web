package insty.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import insty.domain.auth.implement.emailverification.EmailVerificationReader;
import insty.domain.user.dto.request.UserAgreementUpdateReq;
import insty.domain.user.dto.request.UserPasswordUpdateReq;
import insty.domain.user.dto.request.UserTypeUpdateReq;
import insty.domain.user.dto.request.UserUpdateReq;
import insty.domain.user.dto.response.UserDetailRes;
import insty.domain.user.implement.UserFileReader;
import insty.domain.user.implement.UserFileWriter;
import insty.domain.user.implement.UserReader;
import insty.domain.user.implement.UserValidator;
import insty.domain.user.implement.UserWriter;
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
    void 사용자_상세정보_조회에_성공한다() {
        // given
        Long userId = 1L;
        User user = UserFixtureBuilder.getUserWithId(userId);
        String imageUrl = "https://cdn.com/profile.png";

        when(userReader.getUser(userId)).thenReturn(user);
        when(userFileReader.getProfileImageUrl(user)).thenReturn(imageUrl);

        // when
        UserDetailRes result = userService.getDetailUser(userId);

        // then
        verify(userReader).getUser(userId);
        verify(userFileReader).getProfileImageUrl(user);
        assertThat(result).usingRecursiveComparison().isEqualTo(UserDetailRes.from(user, imageUrl));
    }

    @Test
    void 사용자정보수정_프로필만_성공한다() {
        // given
        Long userId = 1L;
        UserUpdateReq userUpdateReq = new UserUpdateReq(
                "new@example.com",
                "newnick",
                "소개글"
        );

        User findUser = UserFixtureBuilder.getUserWithId(userId, "old@example.com", "encodedPassword", "oldnick");
        User updatedUser = UserFixtureBuilder.getUserWithId(userId, userUpdateReq.email(), "encodedPassword", userUpdateReq.nickname());
        String imageUrl = "https://profile.img/default.png";

        when(userReader.getUser(userId)).thenReturn(findUser);

        doNothing().when(userValidator).validateDuplicateEmailExcludingSelf(userId, userUpdateReq.email());
        doNothing().when(userValidator).validateDuplicateNicknameExcludingSelf(userId, userUpdateReq.nickname());

        when(userWriter.updateUser(findUser, userUpdateReq.email(), userUpdateReq.nickname(), userUpdateReq.introduce()))
                .thenReturn(updatedUser);

        when(userFileReader.getProfileImageUrl(updatedUser)).thenReturn(imageUrl);

        // when
        UserDetailRes result = userService.updateUser(userId, userUpdateReq, null);

        // then
        verify(userReader).getUser(userId);
        verify(userValidator).validateDuplicateEmailExcludingSelf(userId, userUpdateReq.email());
        verify(userValidator).validateDuplicateNicknameExcludingSelf(userId, userUpdateReq.nickname());
        verify(userWriter).updateUser(findUser, userUpdateReq.email(), userUpdateReq.nickname(), userUpdateReq.introduce());

        // 비밀번호 변경은 updateUser에서 절대 호출되면 안 됨
        verify(userWriter, never()).changePassword(any(), any());

        assertThat(result.email()).isEqualTo(userUpdateReq.email());
        assertThat(result.nickname()).isEqualTo(userUpdateReq.nickname());
    }

    @Test
    void 사용자_타입_변경_시_업데이트가_정상적으로_동작한다() {
        // given
        Long userId = 1L;
        UserTypeUpdateReq userTypeUpdateReq = new UserTypeUpdateReq(UserType.LEARNER);

        User findUser = UserFixtureBuilder.getUserWithId(userId, "user@example.com", "encodedPassword", "nickname");
        User updatedUser = UserFixtureBuilder.getUserWithId(userId);
        String imageUrl = "https://cdn.com/profile.png";

        when(userReader.getUser(userId)).thenReturn(findUser);
        when(userWriter.changeUserType(findUser, userTypeUpdateReq.userType())).thenReturn(updatedUser);
        when(userFileReader.getProfileImageUrl(updatedUser)).thenReturn(imageUrl);

        // when
        UserDetailRes result = userService.updateUserType(userId, userTypeUpdateReq);

        // then
        verify(userReader).getUser(userId);
        verify(userWriter).changeUserType(findUser, userTypeUpdateReq.userType());
        verify(userFileReader).getProfileImageUrl(updatedUser);
        assertThat(result).usingRecursiveComparison().isEqualTo(UserDetailRes.from(updatedUser, imageUrl));
    }

    @Test
    void 사용자_수신_및_약관_동의_변경이_정상적으로_동작한다() {
        // given
        Long userId = 1L;
        UserAgreementUpdateReq userAgreementUpdateReq = new UserAgreementUpdateReq(true);

        User updatedUser = UserFixtureBuilder.getUserWithId(userId);
        String imageUrl = "https://cdn.com/profile.png";

        when(userReader.getUser(userId)).thenReturn(updatedUser);
        when(userWriter.changeEmailAgreementStatus(updatedUser, userAgreementUpdateReq.isEmailAgree())).thenReturn(updatedUser);
        when(userFileReader.getProfileImageUrl(updatedUser)).thenReturn(imageUrl);

        // when
        UserDetailRes result = userService.updateAgreement(userId, userAgreementUpdateReq);

        // then
        verify(userReader).getUser(userId);
        verify(userWriter).changeEmailAgreementStatus(updatedUser, userAgreementUpdateReq.isEmailAgree());
        verify(userFileReader).getProfileImageUrl(updatedUser);
        assertThat(result).usingRecursiveComparison().isEqualTo(UserDetailRes.from(updatedUser, imageUrl));
    }

    @Test
    void 사용자_비밀번호_변경에_성공한다() {
        // given
        Long userId = 1L;
        UserPasswordUpdateReq userPasswordUpdateReq = new UserPasswordUpdateReq(
                "currentPassword!",
                "newPassword1!"
        );

        User findUser = UserFixtureBuilder.getUserWithId(userId, "user@example.com", "encodedCurrentPassword", "nickname");
        String encodedNewPassword = "encodedNewPassword";

        when(userReader.getUser(userId)).thenReturn(findUser);

        doNothing().when(userValidator).validatePasswordChangeAvailable(findUser.getSocialId());
        doNothing().when(userValidator).validateIdentityByPassword(findUser.getPassword(), userPasswordUpdateReq.currentPassword());
        doNothing().when(userValidator).validateMatchesCurrentPassword(
                findUser.getPassword(),
                userPasswordUpdateReq.currentPassword(),
                userPasswordUpdateReq.newPassword()
        );

        when(bCryptPasswordEncoder.encode(userPasswordUpdateReq.newPassword())).thenReturn(encodedNewPassword);

        // when
        // userService.updatePassword(userId, userPasswordUpdateReq);

        // then
        verify(userReader).getUser(userId);
        verify(userValidator).validatePasswordChangeAvailable(findUser.getSocialId());
        verify(userValidator).validateIdentityByPassword(findUser.getPassword(), userPasswordUpdateReq.currentPassword());
        verify(userValidator).validateMatchesCurrentPassword(
                findUser.getPassword(),
                userPasswordUpdateReq.currentPassword(),
                userPasswordUpdateReq.newPassword()
        );
        verify(bCryptPasswordEncoder).encode(userPasswordUpdateReq.newPassword());
        verify(userWriter).changePassword(findUser, encodedNewPassword);
    }
}
