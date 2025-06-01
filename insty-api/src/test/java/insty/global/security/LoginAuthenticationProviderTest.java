package insty.global.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class LoginAuthenticationProviderTest {

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private LoginAuthenticationProvider authenticationProvider;

    @Test
    void 인증_성공_시_UsernamePasswordAuthenticationToken_리턴() {
        // given
        String rawPassword = "1234";
        String encodedPassword = "$2a$10$";
        CustomUserDetails userDetails = mock(CustomUserDetails.class);

        when(userDetails.getPassword()).thenReturn(encodedPassword);
        when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(userDetails);
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);

        Authentication authRequest = new UsernamePasswordAuthenticationToken("test@example.com", rawPassword);

        // when
        Authentication result = authenticationProvider.authenticate(authRequest);

        // then
        assertTrue(result.isAuthenticated());
        assertEquals(userDetails, result.getPrincipal());
    }

    @Test
    void 비밀번호_불일치시_BadCredentialsException_발생() {
        String rawPassword = "wrong";
        String encodedPassword = "$2a$10$";
        CustomUserDetails userDetails = mock(CustomUserDetails.class);

        when(userDetails.getPassword()).thenReturn(encodedPassword);
        when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(userDetails);
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(false);

        Authentication authRequest = new UsernamePasswordAuthenticationToken("test@example.com", rawPassword);

        assertThrows(BadCredentialsException.class, () -> {
            authenticationProvider.authenticate(authRequest);
        });
    }
}