package insty.domain.community.dto;

import insty.domain.common.FileInfo;
import insty.domain.common.VideoInfo;
import insty.model.community.CommunityAnswer;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public record CommunityAnswerRes(
        Long userId,
        String content,
        List<FileInfo> attachments,
        VideoInfo videoInfo,
        boolean isAccepted,
        Instant createdAt,
        Instant updatedAt

) {
    public static CommunityAnswerRes from(
            CommunityAnswer answer,
            List<FileInfo> attachments,
            VideoInfo videoInfo
    ) {
        return new CommunityAnswerRes(
                answer.getUser().getId(),
                answer.getContent(),
                Optional.ofNullable(attachments).orElse(List.of()),
                videoInfo,
                answer.isAccepted(),
                answer.getCreatedAt(),
                answer.getUpdatedAt()
        );
    }
}
