package insty.social.kakao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NaverUserInfoRes(
    String resultcode,

    String message,

    @JsonProperty("response")
    NaverAccount naverAccount
) {
    public record NaverAccount(
        String id,

        String nickname,

        String email,

        String name
    ) {
    }
}
