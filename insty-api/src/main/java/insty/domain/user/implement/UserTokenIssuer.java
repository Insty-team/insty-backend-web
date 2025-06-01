package insty.domain.user.implement;

import insty.domain.user.dto.UserAuthTokenDto;
import insty.global.security.CustomUserDetails;
import insty.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserTokenIssuer {

    private final JwtUtils jwtHelper;

    public UserAuthTokenDto generateUserTokens(CustomUserDetails user){
        Long userId = user.getUserId();
        // 토큰 생성
        String accessToken = jwtHelper.generateAccessToken(String.valueOf(userId), user.getUserType().name());
        String refreshToken = jwtHelper.generateRefreshToken(String.valueOf(userId));

        // 만료 시간 추출
        long accessTokenExpiresAt = jwtHelper.extractExpiredAt(accessToken);
        long refreshTokenExpiresAt = jwtHelper.extractExpiredAt(refreshToken);

        return UserAuthTokenDto.create(accessToken, refreshToken, accessTokenExpiresAt, refreshTokenExpiresAt);
    }
}
