package insty.domain.community.implement;

import insty.ai.adapter.AiRequester;
import insty.domain.video.repository.VideoCommunityPostRepository;
import insty.domain.video.repository.VideoEncodingRepository;
import insty.error.VideoErrorCode;
import insty.exception.CustomException;
import insty.model.community.CommunityPost;
import insty.model.video.VideoCommunityPost;
import insty.model.video.VideoEncoding;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import insty.s3.adapter.S3FileManager;

@Component
@RequiredArgsConstructor
@Transactional
public class CommunityPostVideoManager {

    private final AiRequester aiRequester;
    private final S3FileManager s3FileManager;
    private final VideoEncodingRepository videoEncodingRepository;
    private final VideoCommunityPostRepository videoCommunityPostRepository;

    public VideoCommunityPost attachVideo(CommunityPost post, UUID videoUuid) {
        if (videoUuid == null) {
            return null;
        }
        VideoCommunityPost video = videoCommunityPostRepository.findByVideoUuid(videoUuid)
                .orElseThrow(() -> new CustomException(VideoErrorCode.VIDEO_NOT_FOUND));
        video.updateCommunityPost(post);
        return videoCommunityPostRepository.save(video);
    }

    public VideoCommunityPost updateAndGetLinkedVideo(CommunityPost post, UUID videoUuid) {
        VideoCommunityPost current = getVideo(post);

        if (videoUuid == null) {
            if (current != null) {
                deleteVideo(post);
            }
            return null;
        }

        if (current != null && current.getVideoUuid().equals(videoUuid)) {
            return current;
        }

        if (current != null) {
            deleteVideo(post);
        }
        return attachVideo(post, videoUuid);
    }

    public VideoCommunityPost getVideo(CommunityPost post) {
        return videoCommunityPostRepository.findByCommunityPostIdAndIsDeleted(post.getId(), false)
                .orElse(null);
    }

    public void deleteVideo(CommunityPost post) {
        VideoCommunityPost video = getVideo(post);
        if (video == null) {
            return;
        }
        VideoEncoding encoding = videoEncodingRepository.findByVideoUuid(video.getVideoUuid())
                .orElseThrow(() -> new CustomException(VideoErrorCode.VIDEO_NOT_FINISHED_ENCODING));
        String directory = encoding.getEncodingVideoDirectoryPath();
        videoCommunityPostRepository.delete(video);
        videoEncodingRepository.delete(encoding);
        aiRequester.deleteAiVideoInfo(video.getVideoUuid());
        s3FileManager.deleteAllByDirectory(directory);
    }
}
