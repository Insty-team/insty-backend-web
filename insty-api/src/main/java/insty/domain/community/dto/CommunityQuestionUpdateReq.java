package insty.domain.community.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record CommunityQuestionUpdateReq(
        Long questionId,
        @NotNull
        String title,
        @NotNull
        String content,
        List<UUID> videoUuids
) {
}
