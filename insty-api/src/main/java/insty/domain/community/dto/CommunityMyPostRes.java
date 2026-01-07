package insty.domain.community.dto;

import insty.model.community.CommunityPost;
import java.time.Instant;

public record CommunityMyPostRes(
        Long postId,
        Long courseId,
        String title,
        String content,
        Instant createdAt,
        Instant updatedAt
) {
    public static CommunityMyPostRes from(CommunityPost post) {
        return new CommunityMyPostRes(
                post.getId(),
                post.getCourse().getId(),
                post.getTitle(),
                post.getContent(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
