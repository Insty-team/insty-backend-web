package insty.domain.community.dto;

import insty.domain.common.FileInfo;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;

public record CommunityAnswerRes(
        @NotNull
        Long userId,
        @NotNull
        String content,
        List<FileInfo> attachments,
        Instant createdAt,
        Instant updatedAt,
        boolean isAccepted
) {
    public static CommunityAnswerRes create(
            @NotNull Long userId,
            @NotNull String content,
            List<FileInfo> attachments,
            Instant createdAt,
            Instant updatedAt,
            boolean isAccepted
    ) {
        return new CommunityAnswerRes(userId, content, attachments, createdAt, updatedAt, isAccepted);
    }

}
