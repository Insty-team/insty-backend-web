package insty.domain.community.dto;

import jakarta.validation.constraints.NotNull;

public record CommunityQuestionRes(
        @NotNull
        String title,
        @NotNull
        String content
) {

    public static CommunityQuestionRes create(
            @NotNull String title,
            @NotNull String content
    ) {
        return new CommunityQuestionRes(title, content);
    }
}
