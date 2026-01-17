package insty.domain.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record CommunityLikeRes(
        @Schema(description = "좋아요 수", example = "10")
        int likeCount,

        @Schema(description = "내가 좋아요를 눌렀는지 여부", example = "true")
        boolean likedByMe
) {
}
