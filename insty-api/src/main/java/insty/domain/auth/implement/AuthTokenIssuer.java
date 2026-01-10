package insty.domain.auth.implement;

import insty.domain.user.dto.UserAuthTokenDto;
import insty.model.user.UserType;
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
    public UserAuthTokenDto generateUserTokens(Long userId, UserType userType){
        // 토큰 생성
        String accessToken = jwtUtils.generateAccessToken(String.valueOf(userId), userType == null ? null : userType.name());
        String refreshToken = jwtUtils.generateRefreshToken(String.valueOf(userId));

        // 만료 시간 추출
        Instant accessTokenExpiresAt = jwtUtils.extractExpiredAt(accessToken);
        Instant refreshTokenExpiresAt = jwtUtils.extractExpiredAt(refreshToken);

        return UserAuthTokenDto.create(accessToken, refreshToken, accessTokenExpiresAt, refreshTokenExpiresAt);
    }
}
