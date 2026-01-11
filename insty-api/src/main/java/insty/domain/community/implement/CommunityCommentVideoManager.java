package insty.domain.community.implement;

import insty.ai.adapter.AiRequester;
import insty.domain.video.repository.VideoCommunityCommentRepository;
import insty.domain.video.repository.VideoEncodingRepository;
import insty.error.VideoErrorCode;
import insty.exception.CustomException;
import insty.model.community.CommunityComment;
import insty.model.video.VideoCommunityComment;
import insty.model.video.VideoEncoding;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import insty.s3.adapter.S3FileManager;

@Component
@RequiredArgsConstructor
@Transactional
public class CommunityCommentVideoManager {

    private final AiRequester aiRequester;
    private final S3FileManager s3FileManager;
    private final VideoEncodingRepository videoEncodingRepository;
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
        VideoEncoding encoding = videoEncodingRepository.findByVideoUuid(video.getVideoUuid())
                .orElseThrow(() -> new CustomException(VideoErrorCode.VIDEO_NOT_FINISHED_ENCODING));
        String directory = encoding.getEncodingVideoDirectoryPath();
        videoCommunityCommentRepository.delete(video);
        videoEncodingRepository.delete(encoding);
        aiRequester.deleteAiVideoInfo(video.getVideoUuid());
        s3FileManager.deleteAllByDirectory(directory);
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
