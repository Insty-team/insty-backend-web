package insty.domain.video.service;

import insty.domain.video.dto.VideoHlsPlaylistReq;
import insty.domain.video.dto.VideoHlsPlaylistRes;
import insty.domain.video.dto.VideoUploadReq;
import insty.domain.video.dto.VideoUploadRes;
import insty.domain.video.implement.VideoIssuer;
import insty.domain.video.implement.VideoReader;
import insty.domain.video.implement.VideoValidator;
import insty.domain.video.implement.VideoWriter;
import insty.model.video.VideoAnswer;
import insty.model.video.VideoCourse;
import insty.s3.dto.PresignedUrlDto;
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
    private final VideoIssuer videoIssuer;

    public VideoUploadRes getPreSignedURLForCourseVideoUpload(VideoUploadReq req) {
        videoValidator.validateContentType(req.fileName(), req.contentType());
        videoValidator.validateUploadable(); // TODO - 메서드 구현

        VideoCourse videoCourse = videoWriter.saveVideoCourse(req);
        PresignedUrlDto presignedUrlDto = videoIssuer.getUploadInfo(videoCourse.getS3Key(), req.contentType());
        return VideoUploadRes.from(videoCourse.getVideoUuid(), presignedUrlDto);
    }

    public VideoUploadRes getPreSignedURLForAnswerVideoUpload(VideoUploadReq req) {
        videoValidator.validateContentType(req.fileName(), req.contentType());
        videoValidator.validateUploadable(); // TODO - 메서드 구현(강의 영상과 다름)

        VideoAnswer videoAnswer = videoWriter.saveVideoAnswer(req);
        PresignedUrlDto presignedUrlDto = videoIssuer.getUploadInfo(videoAnswer.getS3Key(), req.contentType());
        return VideoUploadRes.from(videoAnswer.getVideoUuid(), presignedUrlDto);
    }

    public VideoHlsPlaylistRes getHlsPlaylist(VideoHlsPlaylistReq req) {
        videoValidator.validateReadable(req.type(), req.id());

        UUID videoUuid = videoReader.getVideoUuid(req.type(), req.id());
        String encodingS3Key = videoReader.getEncodingS3Key(videoUuid);
        String signedM3u8Url = videoIssuer.getSignedM3u8Url(encodingS3Key);
        return new VideoHlsPlaylistRes(signedM3u8Url);
    }
}
