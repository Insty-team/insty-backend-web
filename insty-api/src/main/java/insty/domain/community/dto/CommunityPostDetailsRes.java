package insty.domain.community.dto;

import insty.domain.common.FileInfo;
import insty.domain.common.VideoInfo;
import insty.model.community.CommunityPost;
import insty.model.video.VideoCommunityPost;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public record CommunityPostDetailsRes(
        @Schema(description = "게시글 ID", example = "1")
        Long postId,

        @Schema(description = "작성자 정보")
        CommunityUserRes user,

        @Schema(description = "게시글 제목")
        String title,

        @Schema(description = "게시글 내용")
        String content,

        @Schema(description = "첨부 파일 목록")
        List<FileInfo> attachments,

        @Schema(description = "첨부 비디오 정보")
        VideoInfo videoInfo,

        @Schema(description = "작성 시각", example = "2024-01-15T10:30:00Z")
        Instant createdAt,

        @Schema(description = "수정 시각", example = "2024-01-15T10:30:00Z")
        Instant updatedAt
) {
    public static CommunityPostDetailsRes from(CommunityPost post, List<FileInfo> attachments, VideoCommunityPost video) {
        return new CommunityPostDetailsRes(
                post.getId(),
                CommunityUserRes.from(post.getUser()),
                post.getTitle(),
                post.getContent(),
                Optional.ofNullable(attachments).orElse(List.of()),
                VideoInfo.of(video),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
