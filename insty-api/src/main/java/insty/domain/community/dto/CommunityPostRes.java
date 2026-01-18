package insty.domain.community.dto;

import insty.domain.common.FileInfo;
import insty.domain.common.VideoInfo;
import insty.model.community.CommunityPost;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public record CommunityPostRes(
        @Schema(description = "게시글 ID", example = "1")
        Long postId,

        @Schema(description = "작성자 정보")
        CommunityUserRes user,

        @Schema(description = "강좌 ID", example = "1")
        Long courseId,

        @Schema(description = "게시글 제목")
        String title,

        @Schema(description = "게시글 내용")
        String content,

        @Schema(description = "첨부 이미지 목록")
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

    public static CommunityPostRes from(CommunityPost post, List<FileInfo> attachments,
                                        VideoInfo videoInfo) {
        return new CommunityPostRes(
                post.getId(),
                CommunityUserRes.from(post.getUser()),
                post.getCourse().getId(),
                post.getTitle(),
                post.getContent(),
                Optional.ofNullable(attachments).orElse(List.of()),
                videoInfo,
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getLikeCount()
        );
    }
}
