package insty.domain.community.dto;

import jakarta.validation.constraints.NotNull;

public record CommunityQuestionReq(
        @NotNull
        Long id,
        @NotNull
        String content
) {
}
