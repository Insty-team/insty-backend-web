package insty.domain.community.dto;

import jakarta.validation.constraints.NotNull;

public record CommunityAnswerReq(
        @NotNull
        Long questionId,
        @NotNull
        String content
) {
}
