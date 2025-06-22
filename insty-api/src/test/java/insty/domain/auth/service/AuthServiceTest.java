package insty.domain.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import insty.domain.auth.dto.response.AuthUserRes;
import insty.domain.auth.implement.AuthTokenIssuer;
import insty.domain.auth.implement.AuthTokenRedisWriter;
import insty.domain.auth.implement.AuthTokenValidator;
import insty.domain.user.dto.UserAuthTokenDto;
import insty.domain.user.dto.request.UserLoginReq;
import insty.domain.user.implement.UserReader;
import insty.domain.user.implement.UserWriter;
import insty.error.TokenErrorCode;
import insty.exception.CustomException;
import insty.global.security.CustomUserDetails;
import insty.model.user.User;
import insty.model.user.UserFixtureBuilder;
import insty.model.user.UserType;
import insty.util.JwtUtils;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserWriter userWriter;
    @Mock
    private UserReader userReader;
    @Mock
    private AuthTokenIssuer authTokenIssuer;
    @Mock
    private AuthTokenValidator authTokenValidator;
    @Mock
    private AuthTokenRedisWriter authTokenRedisWriter;
    @Mock
    private JwtUtils jwtUtils;

    @Test
    void 이메일_로그인_성공() {
        // Given
        String email = "test@example.com";
        String password = "password";
        UserLoginReq req = new UserLoginReq(email, password, UserType.LEARNER);

        Long userId = 1L;

        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        when(userDetails.getUserId()).thenReturn(userId);
        when(userDetails.getNickname()).thenReturn("nickname");
        when(userDetails.getUserType()).thenReturn(UserType.LEARNER);

        Authentication authenticated = mock(Authentication.class);
        when(authenticated.isAuthenticated()).thenReturn(true);
        when(authenticated.getPrincipal()).thenReturn(userDetails);

        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authenticated);

        Instant accessTokenExpiresAt = Instant.now().plusSeconds(3600);
        Instant refreshTokenExpiresAt = Instant.now().plusSeconds(86400);
        UserAuthTokenDto tokenDto = new UserAuthTokenDto("accessToken", "refreshToken", accessTokenExpiresAt,
                refreshTokenExpiresAt, "Bearer");

        when(authTokenIssuer.generateUserTokens(userId, UserType.LEARNER)).thenReturn(tokenDto);

        // When
        AuthUserRes result = authService.loginByEmail(req);

        // Then
        assertEquals(userId, result.id());
        assertEquals("nickname", result.nickname());
        assertEquals(UserType.LEARNER, result.userType());
        assertEquals("accessToken", result.token().accessToken());
        assertEquals("refreshToken", result.token().refreshToken());

        verify(userWriter).updateLastLoginAt(userId);
        verify(authTokenRedisWriter).saveRefreshToken(userId, "refreshToken");
    }

    @Test
    void 이메일_로그인_실패() {
        // Given
        UserLoginReq req = new UserLoginReq("test@example.com", "wrong", UserType.LEARNER);

        Authentication unauthenticated = mock(Authentication.class);
        when(unauthenticated.isAuthenticated()).thenReturn(false);
        when(authenticationManager.authenticate(any())).thenReturn(unauthenticated);

        // When & Then
        assertThrows(CustomException.class, () -> authService.loginByEmail(req));
    }

    @Test
    void 리프레시_토큰_재발급_성공() {
        // Given
        String refreshToken = "valid.refresh.token";
        Long userId = 1L;

        User user = UserFixtureBuilder.getUserWithId(userId);
        ReflectionTestUtils.setField(user, "userType", UserType.LEARNER); // Spring의 ReflectionTestUtils 사용
        when(jwtUtils.extractSubject(refreshToken)).thenReturn(userId.toString());
        when(userReader.getUser(userId)).thenReturn(user);

        Instant accessTokenExpiresAt = Instant.now().plusSeconds(3600);
        Instant refreshTokenExpiresAt = Instant.now().plusSeconds(86400);
        UserAuthTokenDto tokenDto = new UserAuthTokenDto("accessToken", "refreshToken", accessTokenExpiresAt,
                refreshTokenExpiresAt, "Bearer");
        when(authTokenIssuer.generateUserTokens(userId, UserType.LEARNER)).thenReturn(tokenDto);

        // When
        AuthUserRes res = authService.reissueByRefreshToken(refreshToken);

        // Then
        assertEquals(userId, res.id());
        verify(authTokenValidator).validateRefreshToken(refreshToken);
        verify(authTokenRedisWriter).saveRefreshToken(userId, "refreshToken");
    }

    @Test
    void 리프레시_토큰이_유효하지_않으면_재발급_실패한다() {
        // Given
        String invalidRefreshToken = "invalid.refresh.token";

        // authTokenValidator 가 예외를 던지도록 설정
        doThrow(new CustomException(TokenErrorCode.REFRESH_TOKEN_INVALID))
                .when(authTokenValidator).validateRefreshToken(invalidRefreshToken);

        // When & Then
        assertThrows(CustomException.class, () -> authService.reissueByRefreshToken(invalidRefreshToken));

        // 이후 로직은 절대 호출되면 안 됨
        verify(jwtUtils, never()).extractSubject(any());
        verify(userReader, never()).getUser(any());
        verify(authTokenIssuer, never()).generateUserTokens(any(), eq(UserType.LEARNER));
        verify(authTokenRedisWriter, never()).saveRefreshToken(any(), any());
    }

    @Test
    void 로그아웃_정상처리() {
        // Given
        Long userId = 1L;

        // When
        authService.logout(userId);

        // Then
        verify(authTokenRedisWriter).deleteRefreshToken(userId);
    }

}