package insty.domain.community.dto;

import jakarta.validation.constraints.NotNull;

public record CommunityAnswerReq(
        Long AnswerId,
        @NotNull
        Long questionId,
        @NotNull
        String content
) {
}
