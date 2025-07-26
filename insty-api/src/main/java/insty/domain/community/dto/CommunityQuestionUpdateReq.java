package insty.domain.community.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record CommunityQuestionUpdateReq(
        @NotNull
        String title,
        @NotNull
        String content,
        List<UUID> videoUuids,
        List<UUID> deleteVideoUuids,
        List<Long> deleteFileIds
) {
}
