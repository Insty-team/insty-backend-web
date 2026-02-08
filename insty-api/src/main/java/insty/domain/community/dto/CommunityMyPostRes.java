package insty.domain.community.dto;

import insty.domain.common.FileInfo;
import insty.domain.common.VideoInfo;
import insty.model.community.CommunityPost;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public record CommunityMyPostRes(
        Long postId,
        Long courseId,
        String title,
        String content,
        List<FileInfo> attachments,
        VideoInfo videoInfo,
        Instant createdAt,
        Instant updatedAt
) {
    public static CommunityMyPostRes from(CommunityPost post, List<FileInfo> attachments, VideoInfo videoInfo) {
        return new CommunityMyPostRes(
                post.getId(),
                post.getCourse().getId(),
                post.getTitle(),
                post.getContent(),
                Optional.ofNullable(attachments).orElse(List.of()),
                videoInfo,
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
