package insty.domain.courseqna.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record CourseQnaAcceptAnswerResultRes(
        @Schema(description = "채택된 답변 ID", example = "123")
        Long answerId,

        @Schema(description = "채택 성공 여부", example = "true")
        boolean accepted
) {}