package insty.domain.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record CommunityPostCreateReq(
        @NotBlank
        @Schema(description = "게시글 제목", example = "첫 번째 커뮤니티 글입니다.")
        String title,

        @NotBlank
        @Schema(description = "게시글 내용", example = "커뮤니티 글 내용입니다.")
        String content,

        @Schema(description = "첨부할 비디오 UUID (사전 업로드 필요)", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID videoUuid
) {
}
