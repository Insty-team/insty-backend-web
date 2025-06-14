package insty.social.kakao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoUserInfoRes(
    Long id,

    @JsonProperty("kakao_account")
    KakaoAccount kakaoAccount
) {
    public record KakaoAccount(
        String email,

        @JsonProperty("profile")
        Profile profile
    ) {

        public record Profile(
            String nickname,

            @JsonProperty("thumbnail_image_url")
            String thumbnailImageUrl,

            @JsonProperty("profile_image_url")
            String profileImageUrl
        ) {}
    }
}
