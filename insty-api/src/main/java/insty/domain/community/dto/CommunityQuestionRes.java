package insty.domain.community.dto;

import jakarta.validation.constraints.NotNull;

public record CommunityQuestionRes(
        @NotNull
        String title,
        @NotNull
        String content
) {
}
