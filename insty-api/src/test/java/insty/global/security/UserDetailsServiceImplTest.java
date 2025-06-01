package insty.global.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import insty.domain.user.repository.UserRepository;
import insty.model.user.User;
import java.util.Optional;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void 유저가_존재하면_CustomUserDetails_리턴() {
        // given
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPw")
                .build();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        // when
        UserDetails result = userDetailsService.loadUserByUsername("test@example.com");

        // then
        assertEquals("test@example.com", result.getUsername());
        assertTrue(result instanceof CustomUserDetails);
    }

    @Test
    void 유저가_없으면_UsernameNotFoundException_발생() {
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername("notfound@example.com");
        });
    }
}