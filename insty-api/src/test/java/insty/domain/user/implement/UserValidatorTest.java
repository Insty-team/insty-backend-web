package insty.domain.user.implement;

import static org.junit.jupiter.api.Assertions.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import insty.domain.user.repository.UserRepository;
import insty.error.UserErrorCode;
import insty.exception.CustomException;
import insty.model.user.User;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class UserValidatorTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserValidator userValidator;

    @Test
    void 이메일이_이미_존재하면_예외가_발생한다() {
        // given
        String email = "test@example.com";
        User existingUser = mock(User.class);
        when(userRepository.existsByEmail(email)).thenReturn(true);

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            userValidator.validateDuplicateEmail(email);
        });

        assertThat(exception.getErrorCode()).isEqualTo(UserErrorCode.USER_DUPLICATE_EMAIL);
    }

    @Test
    void 이메일이_존재하지_않으면_예외가_발생하지_않는다() {
        // given
        String email = "unique@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(false);

        // when & then
        assertDoesNotThrow(() -> userValidator.validateDuplicateEmail(email));
    }

    @Test
    void 닉네임이_이미_존재하면_예외가_발생한다() {
        // given
        String nickname = "tester";
        when(userRepository.existsByNickname(nickname)).thenReturn(true);

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            userValidator.validateDuplicateNickname(nickname);
        });

        assertThat(exception.getErrorCode()).isEqualTo(UserErrorCode.USER_DUPLICATE_NICKNAME);
    }

    @Test
    void 닉네임이_존재하지_않으면_예외가_발생하지_않는다() {
        // given
        String nickname = "uniquenick";
        when(userRepository.existsByNickname(nickname)).thenReturn(false);

        // when & then
        assertDoesNotThrow(() -> userValidator.validateDuplicateNickname(nickname));
    }
}
