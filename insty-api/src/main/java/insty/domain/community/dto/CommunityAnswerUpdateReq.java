package insty.domain.community.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CommunityAnswerUpdateReq(
        Long answerId,
        @NotNull
        String content,
        UUID videoUuid
) {
    public static CommunityAnswerUpdateReq create(
            @NotNull Long answerId,
            @NotNull String content
    ) {
        return new CommunityAnswerUpdateReq(answerId, content, null);
    }
    
    public static CommunityAnswerUpdateReq createWithVideo(
            @NotNull Long answerId,
            @NotNull String content,
            UUID videoUuid
    ) {
        return new CommunityAnswerUpdateReq(answerId, content, videoUuid);
    }
}
