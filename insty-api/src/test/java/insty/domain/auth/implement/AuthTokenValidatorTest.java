package insty.domain.auth.implement;

import insty.constants.JwtValidationType;
import insty.exception.CustomException;
import insty.redis.adapter.RedisService;
import insty.util.JwtUtils;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static insty.redis.constant.RedisConstants.REDIS_TOKEN_PREFIX;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthTokenValidatorTest {

    @InjectMocks
    private AuthTokenValidator authTokenValidator;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private RedisService redisService;

    private final String refreshTokenMock = "mock.refresh.token";
    private final Long userId = 123L;
    private final UUID tokenId = UUID.randomUUID();

    @Test
    void 정상적인_리프레시_토큰이면_예외없이_통과한다() {
        // Given & When
        when(jwtUtils.validateToken(refreshTokenMock)).thenReturn(JwtValidationType.VALID);
        when(jwtUtils.extractSubject(refreshTokenMock)).thenReturn(String.valueOf(userId));
        when(redisService.find(REDIS_TOKEN_PREFIX + userId)).thenReturn(Optional.of(refreshTokenMock));
        when(jwtUtils.extractTokenId(refreshTokenMock)).thenReturn(tokenId);

        //  Then
        assertDoesNotThrow(() -> authTokenValidator.validateRefreshToken(refreshTokenMock));
    }

    @Test
    void Redis에_토큰이_없어도_예외없이_통과한다() {
        // Given & When
        when(jwtUtils.validateToken(refreshTokenMock)).thenReturn(JwtValidationType.VALID);
        when(jwtUtils.extractSubject(refreshTokenMock)).thenReturn(String.valueOf(userId));
        when(redisService.find(REDIS_TOKEN_PREFIX + userId)).thenReturn(Optional.empty());

        // Then
        assertDoesNotThrow(() -> authTokenValidator.validateRefreshToken(refreshTokenMock));
    }

    @ParameterizedTest
    @EnumSource(value = JwtValidationType.class, names = {
            "EXPIRED", "CLAIMS_INVALID", "INVALID_SIGNATURE", "MALFORMED", "UNSUPPORTED", "UNKNOWN_ERROR"
    })
    void JWT_유효하지_않은_상태면_예외가_발생한다(JwtValidationType jwtValidationType) {
        // Given & When
        when(jwtUtils.validateToken(refreshTokenMock)).thenReturn(jwtValidationType);

        // Then
        assertThrows(CustomException.class, () -> authTokenValidator.validateRefreshToken(refreshTokenMock));
    }

    @Test
    void Redis_저장된_토큰과_문자열이_다르면_예외가_발생한다() {
        // Given
        String otherRefreshToken = "mock.refresh.token.other";

        // When
        when(jwtUtils.validateToken(otherRefreshToken)).thenReturn(JwtValidationType.VALID);
        when(jwtUtils.extractSubject(otherRefreshToken)).thenReturn(String.valueOf(userId));

        // Redis에는 'refreshTokenMock'이 저장되어 있지만, 'otherRefreshToken'이 요청으로 옴
        when(redisService.find(REDIS_TOKEN_PREFIX + userId))
                .thenReturn(Optional.of(refreshTokenMock)); // Redis에 저장된 토큰은 refreshTokenMock

        // Then
        assertThrows(CustomException.class, () -> authTokenValidator.validateRefreshToken(otherRefreshToken));
    }

    @Test
    void 토큰_문자열은_같지만_토큰ID가_다르면_예외_발생한다() {
        // Given & When
        when(jwtUtils.validateToken(refreshTokenMock)).thenReturn(JwtValidationType.VALID);
        when(jwtUtils.extractSubject(refreshTokenMock)).thenReturn(String.valueOf(userId));
        when(redisService.find(REDIS_TOKEN_PREFIX + userId)).thenReturn(Optional.of(refreshTokenMock));
        // 토큰 ID가 두 번째 호출부터 다르게 반환되도록 설정
        when(jwtUtils.extractTokenId(refreshTokenMock)).thenReturn(tokenId).thenReturn(UUID.randomUUID());

        // Then
        assertThrows(CustomException.class, () -> authTokenValidator.validateRefreshToken(refreshTokenMock));
    }


}
