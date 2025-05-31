package insty.domain.user.dto.response;

import insty.model.user.UserType;

public record UserLoginRes(
        Long id,
        String nickname,
        UserType userType,
        String accessToken,
        String refreshToken,
        long accessTokenExpiresIn,
        long refreshTokenExpiresIn
) {
    public static UserLoginRes create(
            Long id,
            String nickname,
            UserType userType,
            String accessToken,
            String refreshToken,
            long accessTokenExpiresIn,
            long refreshTokenExpiresIn
    ) {
        return new UserLoginRes(id, nickname, userType, accessToken, refreshToken, accessTokenExpiresIn, refreshTokenExpiresIn);
    }
}
