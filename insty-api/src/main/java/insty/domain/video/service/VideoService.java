package insty.domain.video.service;

import insty.constants.VideoConstants;
import insty.domain.user.implement.UserReader;
import insty.domain.video.dto.VideoHlsPlaylistReq;
import insty.domain.video.dto.VideoThumbnailRes;
import insty.domain.video.dto.VideoUploadReq;
import insty.domain.video.dto.VideoUploadRes;
import insty.domain.video.implement.VideoAccessManager;
import insty.domain.video.implement.VideoFileReader;
import insty.domain.video.implement.VideoReader;
import insty.domain.video.implement.VideoValidator;
import insty.domain.video.strategy.VideoStrategyFactory;
import insty.domain.video.strategy.VideoValidateStrategy;
import insty.model.user.User;
import insty.model.video.BaseVideo;
import insty.model.video.VideoEncoding;
import insty.model.video.VideoType;
import insty.s3.dto.PresignedUrlDto;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class VideoService {

    private final VideoStrategyFactory videoStrategyFactory;

    private final VideoValidator videoValidator;
    private final VideoReader videoReader;
    private final VideoAccessManager videoAccessManager;
    private final UserReader userReader;
    private final VideoFileReader videoFileReader;

    public VideoUploadRes getPreSignedURLForVideoUpload(VideoType videoType, Long userId, VideoUploadReq req) {
        videoValidator.validateContentType(req.fileName(), req.contentType());
//        videoStrategyFactory.getValidateStrategy(videoType)
//          .validateUploadable(userId); TODO - 개발 편의를 위해 비활성화

        User user = userReader.getUser(userId);
        BaseVideo video = videoStrategyFactory.getWriteStrategy(videoType).saveVideo(req, user);
        PresignedUrlDto presignedUrlDto = videoAccessManager.getUploadInfo(video.getS3Key(), req.contentType());
        return VideoUploadRes.from(video.getVideoUuid(), presignedUrlDto);
    }

    public VideoThumbnailRes getThumbnailUrl(UUID videoUuid) {
        String thumbnailUrl = videoFileReader.getThumbnailUrl(videoUuid);
        return new VideoThumbnailRes(thumbnailUrl);
    }

    public Map<String, String> getVideoCookieMap(Long userId, VideoHlsPlaylistReq req) {
        VideoValidateStrategy validateStrategy = videoStrategyFactory.getValidateStrategy(req.type());
//        validateStrategy.validateReadable(userId, req.id()); TODO - 개발 편의를 위해 비활성화
        validateStrategy.verifyEncodingCompletedAndDeleted(req.id());

        UUID videoUuid = videoStrategyFactory.getReadStrategy(req.type())
                .getVideoUuid(req.id());
        VideoEncoding videoEncoding = videoReader.getVideoEncoding(videoUuid);

        return videoAccessManager.getSignedCookieMap(videoEncoding.getEncodingVideoDirectoryPath(),
                videoEncoding.getHlsMasterFileKey(), VideoConstants.VIDEO_EXPIRATION_MINUTES);
    }

    public Map<String, String> getPreviewCookieMap(VideoHlsPlaylistReq req) {
        videoStrategyFactory.getValidateStrategy(req.type())
                .verifyEncodingCompletedAndDeleted(req.id());

        UUID videoUuid = videoStrategyFactory.getReadStrategy(req.type())
                .getVideoUuid(req.id());
        VideoEncoding videoEncoding = videoReader.getVideoEncoding(videoUuid);

        return videoAccessManager.getSignedCookieMap(videoEncoding.getPreviewVideoDirectoryPath(),
                videoEncoding.getPreviewMasterFileKey(), VideoConstants.PREVIEW_VIDEO_EXPIRATION_MINUTES);
    }
}
