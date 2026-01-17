package insty.domain.community.dto;

import insty.domain.common.FileInfo;
import insty.domain.common.VideoInfo;
import insty.model.community.CommunityComment;
import insty.model.video.VideoCommunityComment;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public record CommunityCommentRes(
        @Schema(description = "댓글 ID", example = "1")
        Long commentId,

        @Schema(description = "게시글 ID", example = "1")
        Long postId,

        @Schema(description = "작성자 정보")
        CommunityUserRes user,

        @Schema(description = "댓글 내용")
        String content,

        @Schema(description = "첨부 파일 목록")
        List<FileInfo> attachments,

        @Schema(description = "첨부 비디오 정보")
        VideoInfo videoInfo,

        @Schema(description = "작성 시각", example = "2024-01-15T10:30:00Z")
        Instant createdAt,

        @Schema(description = "수정 시각", example = "2024-01-15T10:30:00Z")
        Instant updatedAt,

        @Schema(description = "좋아요 수", example = "10")
        int likeCount
) {
    public static CommunityCommentRes from(CommunityComment comment, List<FileInfo> attachments, VideoCommunityComment video) {
        return new CommunityCommentRes(
                comment.getId(),
                comment.getCommunityPost().getId(),
                CommunityUserRes.from(comment.getUser()),
                comment.getContent(),
                Optional.ofNullable(attachments).orElse(List.of()),
                VideoInfo.of(video),
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                comment.getLikeCount()
        );
    }
}
