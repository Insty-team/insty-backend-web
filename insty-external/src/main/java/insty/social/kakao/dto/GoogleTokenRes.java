package insty.social.kakao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GoogleTokenRes(
    @JsonProperty("access_token")
    String accessToken,

    @JsonProperty("refresh_token")
    String refreshToken,

    @JsonProperty("token_type")
    String tokenType,

    @JsonProperty("expires_in")
    long accessTokenExpiresIn, // 초 단위

    @JsonProperty("refresh_token_expires_in")
    long refreshTokenExpiresIn
) {

}
