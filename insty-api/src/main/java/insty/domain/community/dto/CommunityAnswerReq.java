package insty.domain.community.dto;

import jakarta.validation.constraints.NotNull;

public record CommunityAnswerReq(
        String answerId,
        @NotNull
        String questionId,
        @NotNull
        Long userId,
        @NotNull
        String content
) {
    public static CommunityAnswerReq create(
            @NotNull String questionId,
            @NotNull Long userId,
            @NotNull String content
    ) {
        return new CommunityAnswerReq(null , questionId, userId, content);
    }
}
