package insty.domain.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.UUID;

public record CommunityCommentUpdateReq(
        @NotBlank
        @Schema(description = "댓글 내용", example = "수정된 댓글 내용입니다.")
        String content,

        @Schema(description = "삭제할 첨부 파일 ID 목록", example = "[1]")
        List<Long> deleteFileIds,

        @Schema(description = "첨부할 비디오 UUID (null 이면 기존 영상 삭제)", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID videoUuid
) {
}
