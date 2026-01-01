package insty.domain.community.dto;

import insty.model.community.CommunityComment;
import java.time.Instant;

public record CommunityMyCommentRes(
        Long commentId,
        Long postId,
        Long courseId,
        String content,
        Instant createdAt,
        Instant updatedAt
) {
    public static CommunityMyCommentRes from(CommunityComment comment) {
        return new CommunityMyCommentRes(
                comment.getId(),
                comment.getCommunityPost().getId(),
                comment.getCommunityPost().getCourse().getId(),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}
