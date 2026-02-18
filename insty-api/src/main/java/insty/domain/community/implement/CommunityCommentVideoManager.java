package insty.domain.community.implement;

import insty.domain.video.repository.VideoCommunityCommentRepository;
import insty.error.VideoErrorCode;
import insty.exception.CustomException;
import insty.model.community.CommunityComment;
import insty.model.video.VideoCommunityComment;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional
public class CommunityCommentVideoManager {

    private final VideoCommunityCommentRepository videoCommunityCommentRepository;

    public VideoCommunityComment attachVideo(CommunityComment comment, UUID videoUuid) {
        if (videoUuid == null) {
            return null;
        }
        VideoCommunityComment video = videoCommunityCommentRepository.findByVideoUuid(videoUuid)
                .orElseThrow(() -> new CustomException(VideoErrorCode.VIDEO_NOT_FOUND));
        validateAttachable(video, comment);
        video.updateCommunityComment(comment);
        return videoCommunityCommentRepository.save(video);
    }

    public VideoCommunityComment updateAndGetLinkedVideo(CommunityComment comment, UUID videoUuid) {
        VideoCommunityComment current = getVideo(comment);

        if (videoUuid == null) {
            if (current != null) {
                deleteVideo(comment);
            }
            return null;
        }

        if (current != null && current.getVideoUuid().equals(videoUuid)) {
            return current;
        }

        if (current != null) {
            deleteVideo(comment);
        }

        return attachVideo(comment, videoUuid);
    }

    public VideoCommunityComment getVideo(CommunityComment comment) {
        return videoCommunityCommentRepository.findByCommunityCommentIdAndIsDeleted(comment.getId(), false)
                .orElse(null);
    }

    public void deleteVideo(CommunityComment comment) {
        VideoCommunityComment video = getVideo(comment);
        if (video == null) {
            return;
        }
        video.markAsDeleted();
        videoCommunityCommentRepository.save(video);
    }

    private void validateAttachable(VideoCommunityComment video, CommunityComment targetComment) {
        if (video.isDeleted()) {
            throw new CustomException(VideoErrorCode.VIDEO_NOT_FOUND);
        }
        if (video.getCommunityComment() != null && !video.getCommunityComment().getId().equals(targetComment.getId())) {
            throw new CustomException(VideoErrorCode.VIDEO_ALREADY_ATTACHED);
        }
    }
}
