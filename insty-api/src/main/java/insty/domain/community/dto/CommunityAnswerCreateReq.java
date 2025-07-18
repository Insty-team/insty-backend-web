package insty.domain.community.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CommunityAnswerCreateReq(
        @NotNull
        Long questionId,
        @NotNull
        Long userId,
        @NotNull
        String content,
        UUID videoUuid
) {
    public static CommunityAnswerCreateReq create(
            @NotNull Long questionId,
            @NotNull Long userId,
            @NotNull String content
    ) {
        return new CommunityAnswerCreateReq(questionId, userId, content, null);
    }
    
    public static CommunityAnswerCreateReq createWithVideo(
            @NotNull Long questionId,
            @NotNull Long userId,
            @NotNull String content,
            UUID videoUuid
    ) {
        return new CommunityAnswerCreateReq( questionId, userId, content, videoUuid);
    }
}
