package insty.domain.community.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record CommunityAnswerUpdateReq(
        @NotNull
        Long answerId,
        @NotNull
        String content,
        UUID videoUuid,
        List<Long> deleteFileIds
) {
}
