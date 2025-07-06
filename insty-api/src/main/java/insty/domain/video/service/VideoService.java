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
import insty.domain.video.implement.VideoWriter;
import insty.model.user.User;
import insty.model.video.VideoAnswer;
import insty.model.video.VideoCourse;
import insty.model.video.VideoEncoding;
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

    private final VideoValidator videoValidator;
    private final VideoWriter videoWriter;
    private final VideoReader videoReader;
    private final VideoAccessManager videoAccessManager;
    private final UserReader userReader;
    private final VideoFileReader videoFileReader;

    public VideoUploadRes getPreSignedURLForCourseVideoUpload(Long userId, VideoUploadReq req) {
        videoValidator.validateContentType(req.fileName(), req.contentType());
//        videoValidator.validateVideoCourseUploadable(userId); TODO - 개발 편의를 위해 비활성화

        User user = userReader.getUser(userId);
        VideoCourse videoCourse = videoWriter.saveVideoCourse(req, user);
        PresignedUrlDto presignedUrlDto = videoAccessManager.getUploadInfo(videoCourse.getS3Key(), req.contentType());
        return VideoUploadRes.from(videoCourse.getVideoUuid(), presignedUrlDto);
    }

    public VideoUploadRes getPreSignedURLForAnswerVideoUpload(Long userId, VideoUploadReq req) {
        videoValidator.validateContentType(req.fileName(), req.contentType());
//        videoValidator.validateVideoAnswerUploadable(userId); TODO - 개발 편의를 위해 비활성화

        User user = userReader.getUser(userId);
        VideoAnswer videoAnswer = videoWriter.saveVideoAnswer(req, user);
        PresignedUrlDto presignedUrlDto = videoAccessManager.getUploadInfo(videoAnswer.getS3Key(), req.contentType());
        return VideoUploadRes.from(videoAnswer.getVideoUuid(), presignedUrlDto);
    }

    public VideoThumbnailRes getThumbnailUrl(UUID videoUuid) {
        String thumbnailUrl = videoFileReader.getThumbnailUrl(videoUuid);
        return new VideoThumbnailRes(thumbnailUrl);
    }

    public Map<String, String> getVideoCookieMap(Long userId, VideoHlsPlaylistReq req) {
//        videoValidator.validateReadable(userId, req.type(), req.id()); TODO - 개발 편의를 위해 비활성화
        videoValidator.verifyEncodingCompletedAndDeleted(req.type(), req.id());

        UUID videoUuid = videoReader.getVideoUuid(req.type(), req.id());
        VideoEncoding videoEncoding = videoReader.getVideoEncoding(videoUuid);

        return videoAccessManager.getSignedCookieMap(videoEncoding.getEncodingVideoDirectoryPath(),
                videoEncoding.getHlsMasterFileKey(), VideoConstants.VIDEO_EXPIRATION_MINUTES);
    }

    public Map<String, String> getPreviewCookieMap(VideoHlsPlaylistReq req) {
        videoValidator.verifyEncodingCompletedAndDeleted(req.type(), req.id());

        UUID videoUuid = videoReader.getVideoUuid(req.type(), req.id());
        VideoEncoding videoEncoding = videoReader.getVideoEncoding(videoUuid);

        return videoAccessManager.getSignedCookieMap(videoEncoding.getPreviewVideoDirectoryPath(),
                videoEncoding.getPreviewMasterFileKey(), VideoConstants.PREVIEW_VIDEO_EXPIRATION_MINUTES);
    }
}
