package insty.domain.video.scheduler;

import insty.ai.adapter.AiRequester;
import insty.domain.video.repository.VideoCommunityCommentRepository;
import insty.domain.video.repository.VideoCommunityPostRepository;
import insty.domain.video.repository.VideoEncodingRepository;
import insty.model.video.VideoCommunityComment;
import insty.model.video.VideoCommunityPost;
import insty.model.video.VideoEncoding;
import insty.s3.adapter.S3FileManager;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class VideoResourceCleanupScheduler {

    private final VideoCommunityCommentRepository videoCommunityCommentRepository;
    private final VideoCommunityPostRepository videoCommunityPostRepository;
    private final VideoEncodingRepository videoEncodingRepository;
    private final AiRequester aiRequester;
    private final S3FileManager s3FileManager;
    private final TransactionTemplate transactionTemplate;

    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupDeletedVideoResources() {
        log.info("Starting video resource cleanup job");

        int commentVideoCount = cleanupCommentVideos();
        int postVideoCount = cleanupPostVideos();

        log.info("Video resource cleanup completed. Comment videos: {}, Post videos: {}",
                commentVideoCount, postVideoCount);
    }

    private int cleanupCommentVideos() {
        List<VideoCommunityComment> deletedVideos =
                videoCommunityCommentRepository.findAllByIsDeletedTrue();

        int successCount = 0;
        for (VideoCommunityComment video : deletedVideos) {
            try {
                if (cleanupSingleCommentVideo(video)) {
                    successCount++;
                }
            } catch (Exception e) {
                log.error("Failed to cleanup comment video: videoUuid={}", video.getVideoUuid(), e);
            }
        }

        return successCount;
    }

    private int cleanupPostVideos() {
        List<VideoCommunityPost> deletedVideos =
                videoCommunityPostRepository.findAllByIsDeletedTrue();

        int successCount = 0;
        for (VideoCommunityPost video : deletedVideos) {
            try {
                if (cleanupSinglePostVideo(video)) {
                    successCount++;
                }
            } catch (Exception e) {
                log.error("Failed to cleanup post video: videoUuid={}", video.getVideoUuid(), e);
            }
        }

        return successCount;
    }

    private boolean cleanupSingleCommentVideo(VideoCommunityComment video) {
        VideoEncoding encoding = videoEncodingRepository.findByVideoUuid(video.getVideoUuid())
                .orElse(null);

        if (encoding == null) {
            videoCommunityCommentRepository.delete(video);
            return true;
        }

        String directory = encoding.getEncodingVideoDirectoryPath();
        UUID videoUuid = video.getVideoUuid();

        Boolean dbDeleted = transactionTemplate.execute(status -> {
            try {
                videoCommunityCommentRepository.delete(video);
                videoEncodingRepository.delete(encoding);
                return true;
            } catch (Exception e) {
                status.setRollbackOnly();
                log.error("Failed to delete video from DB: videoUuid={}", videoUuid, e);
                return false;
            }
        });

        if (Boolean.TRUE.equals(dbDeleted)) {
            cleanupExternalResources(videoUuid, directory);
            return true;
        }

        return false;
    }

    private boolean cleanupSinglePostVideo(VideoCommunityPost video) {
        VideoEncoding encoding = videoEncodingRepository.findByVideoUuid(video.getVideoUuid())
                .orElse(null);

        if (encoding == null) {
            videoCommunityPostRepository.delete(video);
            return true;
        }

        String directory = encoding.getEncodingVideoDirectoryPath();
        UUID videoUuid = video.getVideoUuid();

        Boolean dbDeleted = transactionTemplate.execute(status -> {
            try {
                videoCommunityPostRepository.delete(video);
                videoEncodingRepository.delete(encoding);
                return true;
            } catch (Exception e) {
                status.setRollbackOnly();
                log.error("Failed to delete video from DB: videoUuid={}", videoUuid, e);
                return false;
            }
        });

        if (Boolean.TRUE.equals(dbDeleted)) {
            cleanupExternalResources(videoUuid, directory);
            return true;
        }

        return false;
    }

    private void cleanupExternalResources(UUID videoUuid, String directory) {
        try {
            aiRequester.deleteAiVideoInfo(videoUuid);
        } catch (Exception e) {
            log.error("Failed to delete AI video info: videoUuid={}", videoUuid, e);
        }

        try {
            s3FileManager.deleteAllByDirectory(directory);
        } catch (Exception e) {
            log.error("Failed to delete S3 directory: directory={}", directory, e);
        }
    }
}
