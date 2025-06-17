package insty.social.kakao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NaverTokenRes (
    @JsonProperty("access_token")
    String accessToken,

    @JsonProperty("refresh_token")
    String refreshToken,

    @JsonProperty("token_type")
    String tokenType,

    @JsonProperty("expires_in")
    long accessTokenExpiresIn // 초 단위
) {

}
