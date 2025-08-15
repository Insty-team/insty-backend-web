package insty.domain.user.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import insty.domain.course.implement.CourseCleaner;
import insty.domain.user.repository.UserRepository;
import insty.model.user.User;
import insty.model.user.UserFixtureBuilder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class UserWriterTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CourseCleaner courseCleaner;

    @InjectMocks
    private UserWriter userWriter;

    @Test
    void 회원가입_저장() {
        // given
        String email = "test@example.com";
        String password = "securePw123!";
        String nickname = "test";

        User fakeUser = UserFixtureBuilder.getUserWithId(1L, email, password, nickname);

        // mock
        when(userRepository.save(any(User.class))).thenReturn(fakeUser);

        // when
        User savedUser = userWriter.save(email, password, nickname);

        // then
        assertThat(savedUser.getEmail()).isEqualTo(email);
        assertThat(savedUser.getNickname()).isEqualTo(nickname);
        assertThat(savedUser).isSameAs(fakeUser);

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void 유저_삭제() {
        // given
        Long userId = 1L;

        // when
        userWriter.withdraw(userId);

        // then
        verify(userRepository).deleteById(userId);
        verify(courseCleaner).cleanAllData(userId);
    }
}