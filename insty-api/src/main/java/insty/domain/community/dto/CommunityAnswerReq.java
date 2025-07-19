package insty.domain.community.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CommunityAnswerReq(
        String answerId,
        @NotNull
        String questionId,
        @NotNull
        Long userId,
        @NotNull
        String content,
        UUID videoUuid
) {
    public static CommunityAnswerReq create(
            @NotNull String questionId,
            @NotNull Long userId,
            @NotNull String content
    ) {
        return new CommunityAnswerReq(null , questionId, userId, content, null);
    }
    
    public static CommunityAnswerReq createWithVideo(
            @NotNull String questionId,
            @NotNull Long userId,
            @NotNull String content,
            UUID videoUuid
    ) {
        return new CommunityAnswerReq(null, questionId, userId, content, videoUuid);
    }
}
