package insty.domain.user.dto;

import java.time.Instant;

public record UserAuthTokenDto (
    String accessToken,
    String refreshToken,
    Instant accessTokenExpiresAt,
    Instant refreshTokenExpiresAt,
    String tokenType
) {
    public static UserAuthTokenDto create(String accessToken, String refreshToken, Instant accessTokenExpiresAt, Instant refreshTokenExpiresAt) {
        return new UserAuthTokenDto(accessToken, refreshToken, accessTokenExpiresAt, refreshTokenExpiresAt, "Bearer");
    }
}
