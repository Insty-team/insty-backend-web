package insty.domain.community.dto;

import insty.domain.common.FileInfo;
import insty.domain.common.VideoInfo;
import insty.domain.common.dto.UserInfo;
import insty.model.community.CommunityAnswer;
import insty.model.video.VideoAnswer;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public record CommunityAnswerRes(
        @Schema(description = "답변 Id")
        Long answerId,

        @Schema(description = "답변 작성자 정보")
        UserInfo user,

        @Schema(description = "답변 내용", example = "이 문제는 다음과 같이 해결할 수 있습니다.")
        String content,

        @Schema(description = "답변에 첨부된 파일 목록")
        List<FileInfo> attachments,

        @Schema(description = "답변에 첨부된 비디오 정보")
        VideoInfo videoInfo,

        @Schema(description = "답변 채택 여부", example = "false")
        boolean isAccepted,

        @Schema(description = "답변 작성 시간", example = "2024-01-15T10:30:00Z")
        Instant createdAt,

        @Schema(description = "답변 수정 시간", example = "2024-01-15T10:30:00Z")
        Instant updatedAt

) {
    public static CommunityAnswerRes from(
            CommunityAnswer answer,
            List<FileInfo> attachments,
            VideoAnswer video
    ) {
        return new CommunityAnswerRes(
                answer.getId(),
                insty.domain.common.dto.UserInfo.from(answer.getUser()),
                answer.getContent(),
                Optional.ofNullable(attachments).orElse(List.of()),
                VideoInfo.of(video),
                answer.isAccepted(),
                answer.getCreatedAt(),
                answer.getUpdatedAt()
        );
    }
}
