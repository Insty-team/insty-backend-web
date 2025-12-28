package insty.domain.courseqna.dto;

import insty.model.courseqna.QuestionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

public record CourseQuestionMyRes(
        @Schema(description = "질문 id", example = "1")
        Long questionId,

        @Schema(description = "질문 작성자 정보 (id, nickname, userType 포함)")
        CourseUserRes user,

        @Schema(description = "질문이 작성된 강좌 ID", example = "1")
        Long courseId,

        @Schema(description = "질문 제목", example = "자바 스프링 부트 질문입니다.")
        String title,

        @Schema(description = "질문 내용", example = "스프링 부트에서 JPA를 사용할 때 발생하는 문제입니다.")
        String content,

        @Schema(description = "질문 상태 (WAITING: 답변 대기, ANSWERED: 답변 있음, ACCEPTED: 답변 채택됨)", example = "WAITING")
        QuestionStatus status,

        @Schema(description = "답변 개수", example = "2")
        int answerCount,

        @Schema(description = "새로운 답변 여부", example = "false")
        Boolean hasNewAnswer,

        @Schema(description = "질문 작성 시간 (UTC ISO8601)", example = "2024-01-15T10:30:00Z")
        Instant createdAt,

        @Schema(description = "질문 수정 시간 (UTC ISO8601)", example = "2024-01-15T10:30:00Z")
        Instant updatedAt
) {

    public static CourseQuestionMyRes from(
            CourseQuestionSearchInfo info,
            Long answerCount,
            boolean hasNewAnswer
    ) {
        return new CourseQuestionMyRes(
                info.id(),
                CourseUserRes.from(info.user()),
                info.courseId(),
                info.title(),
                info.content(),
                info.status(),
                answerCount.intValue(),
                hasNewAnswer,
                info.createdAt(),
                info.updatedAt()
        );
    }
}
