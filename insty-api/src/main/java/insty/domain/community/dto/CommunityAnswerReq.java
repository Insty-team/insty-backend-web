package insty.domain.community.dto;

import jakarta.validation.constraints.NotNull;

public record CommunityAnswerReq(
        Long answerId,
        @NotNull
        Long questionId,
        @NotNull
        Long userId,
        @NotNull
        String content
) {
}
