package insty.domain.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record CommunityCommentCreateReq(
        @NotBlank
        @Schema(description = "댓글 내용", example = "댓글 내용입니다.")
        String content,

        @Schema(description = "첨부할 비디오 UUID (사전 업로드 필요)", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID videoUuid
) {
}
