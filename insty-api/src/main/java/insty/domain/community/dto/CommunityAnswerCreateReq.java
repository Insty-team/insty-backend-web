package insty.domain.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Builder;

@Builder
public record CommunityAnswerCreateReq(
        @NotNull
        @Schema(description = "답변 내용", example = "이 문제는 다음과 같이 해결할 수 있습니다.")
        String content,

        @Schema(description = "답변에 첨부할 비디오 UUID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID videoUuid
) {
}
