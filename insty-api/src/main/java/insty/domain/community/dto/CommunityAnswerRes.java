package insty.domain.community.dto;

import insty.domain.common.FileInfo;
import insty.model.community.CommunityAnswer;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public record CommunityAnswerRes(
        Long userId,
        String content,
        List<FileInfo> attachments,
        Instant createdAt,
        Instant updatedAt,
        boolean isAccepted
) {
    public static CommunityAnswerRes from(
            CommunityAnswer answer,
            List<FileInfo> attachments
    ) {
        return new CommunityAnswerRes(
                answer.getUser().getId(),
                answer.getContent(),
                Optional.ofNullable(attachments).orElse(List.of()),
                answer.getCreatedAt(),
                answer.getUpdatedAt(),
                answer.isAccepted()
        );
    }
}
