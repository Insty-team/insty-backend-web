package insty.domain.courseqna.dto;

import insty.domain.common.FileInfo;
import insty.domain.common.VideoInfo;
import insty.model.courseqna.CourseQuestion;
import insty.model.courseqna.QuestionStatus;
import insty.model.video.VideoQuestion;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public record CommunityQuestionDetailsRes(
        @Schema(description = "질문 id", example = "1")
        Long questionId,

        @Schema(description = "질문 작성자 정보 (id, nickname, userType 포함)")
        CommunityUserRes user,

        @Schema(description = "질문이 작성된 강좌 ID", example = "1")
        Long courseId,

        @Schema(description = "질문 제목", example = "자바 스프링 부트 질문입니다.")
        String title,

        @Schema(description = "질문 내용", example = "스프링 부트에서 JPA를 사용할 때 발생하는 문제입니다.")
        String content,

        @Schema(description = "질문 상태 (WAITING: 답변 대기, ANSWERED: 답변 있음, ACCEPTED: 답변 채택됨)", example = "WAITING")
        QuestionStatus status,

        @Schema(description = "질문에 첨부된 파일 목록 (id, name=원본파일명, contentType, size, url 포함)")
        List<FileInfo> attachments,

        @Schema(description = "질문에 첨부된 비디오 정보 (videoUuid, originFileName 등)")
        VideoInfo videoInfo,

        @Schema(description = "질문 작성 시간 (UTC ISO8601)", example = "2024-01-15T10:30:00Z")
        Instant createdAt,

        @Schema(description = "질문 수정 시간 (UTC ISO8601)", example = "2024-01-15T10:30:00Z")
        Instant updatedAt
) {
    public static CommunityQuestionDetailsRes from(
            CourseQuestion question,
            List<FileInfo> attachments,
            VideoQuestion video
    ) {
        return new CommunityQuestionDetailsRes(
                question.getId(),
                CommunityUserRes.from(question.getUser()),
                question.getCourse().getId(),
                question.getTitle(),
                question.getContent(),
                question.getStatus(),
                Optional.ofNullable(attachments).orElse(List.of()),
                VideoInfo.of(video),
                question.getCreatedAt(),
                question.getUpdatedAt()
        );
    }
}
