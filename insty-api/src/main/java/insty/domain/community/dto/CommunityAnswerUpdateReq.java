package insty.domain.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record CommunityAnswerUpdateReq(
        @NotNull
        @Schema(description = "답변 내용", example = "이 문제는 다음과 같이 해결할 수 있습니다.")
        String content,

        @Schema(description = "답변에 첨부할 비디오 UUID (null이면 기존 영상 삭제)", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID videoUuid,

        @Schema(description = "삭제할 파일 ID 목록 (첨부 제한 1개 기준으로 최종 개수 검증)", example = "[1, 2, 3]")
        List<Long> deleteFileIds
) {
}
