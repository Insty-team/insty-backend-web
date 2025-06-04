package insty.domain.community.dto;

import jakarta.validation.constraints.NotNull;

public record CommunityQuestionReq(
        @NotNull
        Long courseId,
        @NotNull
        String title,
        @NotNull
        String content
) {
}
