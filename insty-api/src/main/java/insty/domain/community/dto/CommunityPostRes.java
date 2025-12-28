package insty.domain.community.dto;

import insty.model.community.CommunityPost;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

public record CommunityPostRes(
        @Schema(description = "게시글 ID", example = "1")
        Long postId,

        @Schema(description = "작성자 정보")
        CommunityUserRes user,

        @Schema(description = "게시글 제목")
        String title,

        @Schema(description = "게시글 내용")
        String content,

        @Schema(description = "작성 시각", example = "2024-01-15T10:30:00Z")
        Instant createdAt,

        @Schema(description = "수정 시각", example = "2024-01-15T10:30:00Z")
        Instant updatedAt
) {
    public static CommunityPostRes from(CommunityPost post) {
        return new CommunityPostRes(
                post.getId(),
                CommunityUserRes.from(post.getUser()),
                post.getTitle(),
                post.getContent(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
