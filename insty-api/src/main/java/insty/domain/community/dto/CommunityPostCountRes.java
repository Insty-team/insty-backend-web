package insty.domain.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record CommunityPostCountRes(
        @Schema(description = "커뮤니티 게시글 수", example = "123")
        long count
) {
    public static CommunityPostCountRes of(long count) {
        return new CommunityPostCountRes(count);
    }
}
