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
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class VideoResourceCleanupScheduler {

    private final VideoCommunityCommentRepository videoCommunityCommentRepository;
    private final VideoCommunityPostRepository videoCommunityPostRepository;
    private final VideoEncodingRepository videoEncodingRepository;
    private final AiRequester aiRequester;
    private final S3FileManager s3FileManager;

    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
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

        for (VideoCommunityComment video : deletedVideos) {
            try {
                cleanupVideoResources(video.getVideoUuid());
                videoCommunityCommentRepository.delete(video);
            } catch (Exception e) {
                log.error("Failed to cleanup comment video resources: videoUuid={}",
                        video.getVideoUuid(), e);
            }
        }

        return deletedVideos.size();
    }

    private int cleanupPostVideos() {
        List<VideoCommunityPost> deletedVideos =
                videoCommunityPostRepository.findAllByIsDeletedTrue();

        for (VideoCommunityPost video : deletedVideos) {
            try {
                cleanupVideoResources(video.getVideoUuid());
                videoCommunityPostRepository.delete(video);
            } catch (Exception e) {
                log.error("Failed to cleanup post video resources: videoUuid={}",
                        video.getVideoUuid(), e);
            }
        }

        return deletedVideos.size();
    }

    private void cleanupVideoResources(UUID videoUuid) {
        videoEncodingRepository.findByVideoUuid(videoUuid).ifPresent(encoding -> {
            String directory = encoding.getEncodingVideoDirectoryPath();
            videoEncodingRepository.delete(encoding);

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
        });
    }
}
