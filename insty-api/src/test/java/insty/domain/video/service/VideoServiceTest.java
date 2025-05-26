package insty.domain.video.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

import insty.cloudfront.adapter.CloudFrontSigner;
import insty.domain.video.dto.VideoUploadReq;
import insty.domain.video.dto.VideoUploadRes;
import insty.domain.video.implement.VideoIssuer;
import insty.domain.video.implement.VideoValidator;
import insty.domain.video.implement.VideoWriter;
import insty.domain.video.repository.VideoAnswerRepository;
import insty.domain.video.repository.VideoCourseRepository;
import insty.global.property.AppProperties;
import insty.model.video.AnalysisStatus;
import insty.model.video.EncodingStatus;
import insty.model.video.VideoAnswer;
import insty.model.video.VideoCourse;
import insty.model.video.VideoType;
import insty.s3.adapter.S3UrlIssuer;
import insty.s3.constant.S3Constants;
import insty.s3.dto.PresignedUrlDto;
import insty.uuid.UuidProvider;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Tag("integration")
@SpringBootTest
@ActiveProfiles("test")
class VideoServiceTest {

    @Autowired
    private VideoService videoService;

    @Autowired
    private VideoValidator videoValidator;
    @Autowired
    private VideoWriter videoWriter;
    @MockitoBean
    private UuidProvider uuidProvider;
    @Autowired
    private VideoIssuer videoIssuer;
    @Autowired
    private VideoCourseRepository videoCourseRepository;
    @Autowired
    private VideoAnswerRepository videoAnswerRepository;
    @MockitoBean
    private S3UrlIssuer s3UrlIssuer;
    @MockitoBean
    private CloudFrontSigner cloudFrontSigner;
    @MockitoBean
    private AppProperties appProperties;

    @Test
    void getPreSignedURLForCourseVideoUpload_정상() {
        // given
        String fileName = "fileName.mp4";
        String contentType = "video/mp4";
        VideoUploadReq req = new VideoUploadReq(fileName, contentType);

        // mock
        UUID fixedUuid = UUID.fromString("00000000-0000-0000-0000-000000000001");
        when(uuidProvider.generate())
                .thenReturn(fixedUuid);

        String s3Key = "vod/" + VideoType.COURSE + "/mp4/" + fixedUuid + "/" + fileName;
        String presignedUrl = "https://s3.ap-northeast-2.amazonaws.com/test-bucket/" + s3Key + "?...";
        Instant expiredAt = Instant.now().plus(Duration.ofMinutes(S3Constants.UPLOAD_URL_EXPIRATION_MINUTES));
        when(s3UrlIssuer.generatePresignedUrl(anyString(), anyString()))
                .thenReturn(new PresignedUrlDto(
                        presignedUrl,
                        expiredAt
                ));

        // when
        VideoUploadRes res = videoService.getPreSignedURLForCourseVideoUpload(req);

        // then
        assertThat(res).isNotNull();
        assertThat(res.uuid()).isEqualTo(fixedUuid);
        assertThat(res.uploadUrl()).isEqualTo(presignedUrl);
        assertThat(res.expiredAt()).isEqualTo(expiredAt);

        Optional<VideoCourse> optional = videoCourseRepository.findByVideoUuid(fixedUuid);
        assertThat(optional).isPresent();

        VideoCourse videoCourse = optional.get();
        assertThat(videoCourse.getId()).isNotNull();
        assertThat(videoCourse.getVideoUuid()).isEqualTo(fixedUuid);
        assertThat(videoCourse.getS3Key()).isEqualTo(s3Key);
        assertThat(videoCourse.getExtension()).isEqualTo("mp4");
        assertThat(videoCourse.getOriginalFileName()).isEqualTo(fileName);
        assertThat(videoCourse.getThumbnailUrl()).isNull();
        assertThat(videoCourse.getEncodingStatus()).isEqualTo(EncodingStatus.PROCESSING);
        assertThat(videoCourse.getEncodingAt()).isNotNull();
        assertThat(videoCourse.getAnalysisStatus()).isEqualTo(AnalysisStatus.WAITING);
        assertThat(videoCourse.getAnalysisAt()).isNull();
    }

    @Test
    void getPreSignedURLForAnswerVideoUpload_정상() {
        // given
        String fileName = "fileName.mp4";
        String contentType = "video/mp4";
        VideoUploadReq req = new VideoUploadReq(fileName, contentType);

        // mock
        UUID fixedUuid = UUID.fromString("00000000-0000-0000-0000-000000000001");
        when(uuidProvider.generate())
                .thenReturn(fixedUuid);

        String s3Key = "vod/" + VideoType.ANSWER + "/mp4/" + fixedUuid + "/" + fileName;
        String presignedUrl = "https://s3.ap-northeast-2.amazonaws.com/test-bucket/" + s3Key + "?...";
        Instant expiredAt = Instant.now().plus(Duration.ofMinutes(S3Constants.UPLOAD_URL_EXPIRATION_MINUTES));
        when(s3UrlIssuer.generatePresignedUrl(anyString(), anyString()))
                .thenReturn(new PresignedUrlDto(
                        presignedUrl,
                        expiredAt
                ));

        // when
        VideoUploadRes res = videoService.getPreSignedURLForAnswerVideoUpload(req);

        // then
        assertThat(res).isNotNull();
        assertThat(res.uuid()).isEqualTo(fixedUuid);
        assertThat(res.uploadUrl()).isEqualTo(presignedUrl);
        assertThat(res.expiredAt()).isEqualTo(expiredAt);

        Optional<VideoAnswer> optional = videoAnswerRepository.findByVideoUuid(fixedUuid);
        assertThat(optional).isPresent();

        VideoAnswer videoAnswer = optional.get();
        assertThat(videoAnswer.getId()).isNotNull();
        assertThat(videoAnswer.getVideoUuid()).isEqualTo(fixedUuid);
        assertThat(videoAnswer.getS3Key()).isEqualTo(s3Key);
        assertThat(videoAnswer.getExtension()).isEqualTo("mp4");
        assertThat(videoAnswer.getOriginalFileName()).isEqualTo(fileName);
        assertThat(videoAnswer.getThumbnailUrl()).isNull();
        assertThat(videoAnswer.getEncodingStatus()).isEqualTo(EncodingStatus.PROCESSING);
        assertThat(videoAnswer.getEncodingAt()).isNotNull();
    }
}