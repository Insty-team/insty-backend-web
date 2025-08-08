package insty.domain.community.dto;

import insty.domain.common.FileInfo;
import insty.domain.common.VideoInfo;
import insty.model.community.CommunityQuestion;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public record CommunityQuestionDetailsRes(
        @Schema(description = "질문 작성자 정보")
        CommunityUserRes user,

        @Schema(description = "질문이 작성된 강좌 ID", example = "1")
        Long courseId,

        @Schema(description = "질문 제목", example = "자바 스프링 부트 질문입니다.")
        String title,

        @Schema(description = "질문 내용", example = "스프링 부트에서 JPA를 사용할 때 발생하는 문제입니다.")
        String content,

        @Schema(description = "질문에 달린 답변 목록")
        List<CommunityAnswerRes> answers,

        @Schema(description = "질문에 첨부된 파일 목록")
        List<FileInfo> attachments,

        @Schema(description = "질문에 첨부된 비디오 정보")
        VideoInfo videoInfo,

        @Schema(description = "질문 작성 시간", example = "2024-01-15T10:30:00Z")
        Instant createdAt,

        @Schema(description = "질문 수정 시간", example = "2024-01-15T10:30:00Z")
        Instant updatedAt
) {
    public static CommunityQuestionDetailsRes from(
            CommunityQuestion question,
            List<FileInfo> attachments,
            VideoInfo videoInfo,
            List<CommunityAnswerRes> answers
    ) {
        return new CommunityQuestionDetailsRes(
                CommunityUserRes.from(question.getUser()),
                question.getCourse().getId(),
                question.getTitle(),
                question.getContent(),
                Optional.ofNullable(answers).orElse(List.of()),
                Optional.ofNullable(attachments).orElse(List.of()),
                videoInfo,
                question.getCreatedAt(),
                question.getUpdatedAt()
        );
    }
}
