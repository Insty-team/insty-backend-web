package insty.domain.user.dto.response;

import insty.model.user.UserType;

public record LoginSuccessRes(
        Long id,
        String nickname,
        UserType userType,
        String accessToken,
        String refreshToken,
        long accessTokenExpiresIn,
        long refreshTokenExpiresIn
) {
    public static LoginSuccessRes create(
            Long id,
            String nickname,
            UserType userType,
            String accessToken,
            String refreshToken,
            long accessTokenExpiresIn,
            long refreshTokenExpiresIn
    ) {
        return new LoginSuccessRes(id, nickname, userType, accessToken, refreshToken, accessTokenExpiresIn, refreshTokenExpiresIn);
    }
}
