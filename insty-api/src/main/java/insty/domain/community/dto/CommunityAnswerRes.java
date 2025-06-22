package insty.domain.community.dto;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record CommunityAnswerRes(
        @NotNull
        Long userId,
        @NotNull
        String questionId,
        @NotNull
        String content,
        Instant createdAt,
        Instant updatedAt
) {
    public static CommunityAnswerRes create(
            @NotNull Long userId,
            @NotNull String questionId,
            @NotNull String content,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new CommunityAnswerRes(userId, questionId, content, createdAt, updatedAt);
    }

}
