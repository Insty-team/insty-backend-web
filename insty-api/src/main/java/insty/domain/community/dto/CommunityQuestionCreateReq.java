package insty.domain.community.dto;

import insty.model.community.CommunityBoardType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CommunityQuestionCreateReq(

        @NotNull
        @Schema(description = "질문을 작성할 강좌 ID", example = "1")
        Long courseId,

        @NotNull
        @Schema(description = "질문 제목", example = "자바 스프링 부트 질문입니다.")
        String title,

        @NotNull
        @Schema(description = "질문 내용", example = "스프링 부트에서 JPA를 사용할 때 발생하는 문제입니다.")
        String content,

        @Schema(description = "게시판 타입(QNA / COMMUNITY)", example = "QNA")
        CommunityBoardType boardType,

        @Schema(description = "질문에 첨부할 비디오 UUID (사전 업로드/인코딩 완료 필요)", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID videoUuid
) {
}