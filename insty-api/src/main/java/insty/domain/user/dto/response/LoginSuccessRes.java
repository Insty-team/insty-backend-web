package insty.domain.user.dto.response;

public record LoginSuccessRes(
        String accessToken,
        String refreshToken,
        long accessTokenExpiresIn,
        long refreshTokenExpiresIn
) {
    public static LoginSuccessRes create(
            String accessToken,
            String refreshToken,
            long accessTokenExpiresIn,
            long refreshTokenExpiresIn
    ) {
        return new LoginSuccessRes(accessToken, refreshToken, accessTokenExpiresIn, refreshTokenExpiresIn);
    }
}
