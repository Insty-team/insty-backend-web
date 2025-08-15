package insty.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import insty.ai.adapter.AiRequester;
import insty.domain.auth.util.TokenExtractor;
import insty.domain.user.dto.request.UserCreateReq;
import insty.domain.user.dto.request.UserPasswordUpdateReq;
import insty.domain.user.dto.response.UserCreateRes;
import insty.domain.user.dto.response.UserDetailRes;
import insty.domain.user.implement.UserFileReader;
import insty.domain.user.implement.UserReader;
import insty.domain.user.implement.UserValidator;
import insty.domain.user.implement.UserWriter;
import insty.model.user.User;
import insty.model.user.UserFixtureBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private UserWriter userWriter;
    @Mock
    private UserValidator userValidator;
    @Mock
    private UserReader userReader;
    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Mock
    private UserFileReader userFileReader;
    @Mock
    private TokenExtractor tokenExtractor;
    @Mock
    private AiRequester aiRequester;

    @InjectMocks
    private AccountService accountService;

    @Test
    void 회원가입시_이메일_닉네임_중복검사를_수행하고_비밀번호를_암호화하여_저장한다() {
        // given
        UserCreateReq req = new UserCreateReq("test@example.com", "plainPassword", "nickname");
        String encodedPassword = "encodedPassword";

        User savedUser = UserFixtureBuilder.getUserWithId(1L, req.email(), encodedPassword, req.nickname());

        when(bCryptPasswordEncoder.encode(req.password())).thenReturn(encodedPassword);
        when(userWriter.save(req.email(), encodedPassword, req.nickname())).thenReturn(savedUser);

        // when
        UserCreateRes result = accountService.signup(req);

        // then
        verify(userValidator).validateEmailVerification(req.email());
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
        UserDetailRes result = accountService.updatePassword(userId, req);

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
    void 사용자_탈퇴시_존재여부를_확인하고_탈퇴한다() {
        // given
        String token = "valid-token";
        when(tokenExtractor.getCurrentToken()).thenReturn(token);
        when(aiRequester.deleteUserData(anyString(), anyLong())).thenReturn(Mono.empty());
        Long userId = 1L;

        // when
        accountService.withdraw(userId);

        // then
        verify(userReader).validateUserExists(eq(userId));
        verify(userWriter).withdraw(eq(userId));
        verify(aiRequester).deleteUserData(token, userId);
    }
}