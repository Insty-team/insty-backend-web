package insty.domain.video.service;

import insty.domain.video.dto.VideoHlsPlaylistReq;
import insty.domain.video.dto.VideoUploadReq;
import insty.domain.video.dto.VideoUploadRes;
import insty.domain.video.implement.VideoAccessManager;
import insty.domain.video.implement.VideoReader;
import insty.domain.video.implement.VideoValidator;
import insty.domain.video.implement.VideoWriter;
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

    public VideoUploadRes getPreSignedURLForCourseVideoUpload(VideoUploadReq req) {
        videoValidator.validateContentType(req.fileName(), req.contentType());
        videoValidator.validateUploadable(); // TODO - 메서드 구현

        VideoCourse videoCourse = videoWriter.saveVideoCourse(req);
        PresignedUrlDto presignedUrlDto = videoAccessManager.getUploadInfo(videoCourse.getS3Key(), req.contentType());
        return VideoUploadRes.from(videoCourse.getVideoUuid(), presignedUrlDto);
    }

    public VideoUploadRes getPreSignedURLForAnswerVideoUpload(VideoUploadReq req) {
        videoValidator.validateContentType(req.fileName(), req.contentType());
        videoValidator.validateUploadable(); // TODO - 메서드 구현(강의 영상과 다름)

        VideoAnswer videoAnswer = videoWriter.saveVideoAnswer(req);
        PresignedUrlDto presignedUrlDto = videoAccessManager.getUploadInfo(videoAnswer.getS3Key(), req.contentType());
        return VideoUploadRes.from(videoAnswer.getVideoUuid(), presignedUrlDto);
    }

    public Map<String, String> getSignedCookieMap(VideoHlsPlaylistReq req) {
        videoValidator.validateReadable(req.type(), req.id()); // TODO - 메서드 구현

        UUID videoUuid = videoReader.getVideoUuid(req.type(), req.id());
        VideoEncoding videoEncoding = videoReader.getVideoEncoding(videoUuid);

        return videoAccessManager.getSignedCookieMap(videoEncoding.getEncodingVideoDirectoryPath(),
                videoEncoding.getHlsMasterFileKey());
    }
}
