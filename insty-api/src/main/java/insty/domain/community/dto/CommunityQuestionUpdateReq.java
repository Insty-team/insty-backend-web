package insty.domain.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record CommunityQuestionUpdateReq(
        @NotNull
        @Schema(description = "질문 제목", example = "자바 스프링 부트 질문입니다.")
        String title,

        @NotNull
        @Schema(description = "질문 내용", example = "스프링 부트에서 JPA를 사용할 때 발생하는 문제입니다.")
        String content,

        @Schema(description = "질문에 첨부할 비디오 UUID (null이면 기존 영상 유지)", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID videoUuid,

        @Schema(description = "삭제할 파일 ID 목록 (추가 업로드와 합산해 최종 2개 제한 적용)", example = "[1, 2, 3]")
        List<Long> deleteFileIds
) {
}
