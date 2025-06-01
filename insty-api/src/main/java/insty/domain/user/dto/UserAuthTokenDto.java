package insty.domain.user.dto;

public record UserAuthTokenDto (
    String accessToken,
    String refreshToken,
    long accessTokenExpiresAt,
    long refreshTokenExpiresAt,
    String tokenType
) {
    public static UserAuthTokenDto create(String accessToken, String refreshToken, long accessTokenExpiresAt, long refreshTokenExpiresAt) {
        return new UserAuthTokenDto(accessToken, refreshToken, accessTokenExpiresAt, refreshTokenExpiresAt, "Bearer");
    }
}
