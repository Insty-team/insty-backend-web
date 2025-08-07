package insty.domain.community.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Builder;

@Builder
public record CommunityAnswerCreateReq(
        @NotNull
        String content,
        UUID videoUuid
) {
}
