package insty.domain.community.dto;

import jakarta.validation.constraints.NotNull;

public record CommunityAnswerRes(
        @NotNull
        String content
) {
    public static CommunityAnswerRes create(
            @NotNull String content
    ) {
        return new CommunityAnswerRes(content);
    }
}
