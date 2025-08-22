package insty.domain.community.dto;

import insty.model.community.CommunityQuestion;
import insty.model.community.QuestionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;


public record CommunityQuestionRes(
        @Schema(description = "질문 작성자 정보 (id, nickname, userType 포함)")
        CommunityUserRes user,

        @Schema(description = "질문이 작성된 강좌 ID", example = "1")
        Long courseId,

        @Schema(description = "질문 제목", example = "자바 스프링 부트 질문입니다.")
        String title,

        @Schema(description = "질문 내용", example = "스프링 부트에서 JPA를 사용할 때 발생하는 문제입니다.")
        String content,

        @Schema(description = "질문 상태", example = "WAITING", allowableValues = {"WAITING", "ANSWERED", "ACCEPTED"})
        QuestionStatus status,

        @Schema(description = "질문 작성 시간 (UTC ISO8601)", example = "2024-01-15T10:30:00Z")
        Instant createdAt,

        @Schema(description = "질문 수정 시간 (UTC ISO8601)", example = "2024-01-15T10:30:00Z")
        Instant updatedAt
) {
    public static CommunityQuestionRes from(
            CommunityQuestion question
    ) {
        return new CommunityQuestionRes(
                CommunityUserRes.from(question.getUser()),
                question.getCourse().getId(),
                question.getTitle(),
                question.getContent(),
                question.getStatus(),
                question.getCreatedAt(),
                question.getUpdatedAt()
        );
    }

    public static CommunityQuestionRes from(
            CommunityQuestionSearchInfo info
    ) {
        return new CommunityQuestionRes(
                CommunityUserRes.from(info.user()),
                info.courseId(),
                info.title(),
                info.content(),
                info.status(),
                info.createdAt(),
                info.updatedAt()
        );
    }
}
