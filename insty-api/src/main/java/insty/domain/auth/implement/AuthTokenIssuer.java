package insty.domain.auth.implement;

import insty.domain.user.dto.UserAuthTokenDto;
import insty.util.JwtUtils;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthTokenIssuer {

    private final JwtUtils jwtUtils;

    /**
     * 토큰 발급
     */
    public UserAuthTokenDto generateUserTokens(Long userId){
        // 토큰 생성
        String accessToken = jwtUtils.generateAccessToken(String.valueOf(userId));
        String refreshToken = jwtUtils.generateRefreshToken(String.valueOf(userId));

        // 만료 시간 추출
        Instant accessTokenExpiresAt = jwtUtils.extractExpiredAt(accessToken);
        Instant refreshTokenExpiresAt = jwtUtils.extractExpiredAt(refreshToken);

        return UserAuthTokenDto.create(accessToken, refreshToken, accessTokenExpiresAt, refreshTokenExpiresAt);
    }
}
