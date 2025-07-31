package insty.domain.community.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Builder;

@Builder
public record CommunityQuestionCreateReq(
        @NotNull
        Long courseId,
        @NotNull
        String title,
        @NotNull
        String content,
        UUID videoUuid
) {
}
