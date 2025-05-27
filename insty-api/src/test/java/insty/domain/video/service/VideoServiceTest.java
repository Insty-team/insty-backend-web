package insty.domain.video.service;

import static insty.cloudfront.constant.CloudFrontConstants.CLOUDFRONT_KEY_PAIR_ID;
import static insty.cloudfront.constant.CloudFrontConstants.CLOUDFRONT_POLICY;
import static insty.cloudfront.constant.CloudFrontConstants.CLOUDFRONT_SIGNATURE;
import static insty.cloudfront.constant.CloudFrontConstants.CLOUDFRONT_SIGNED_MASTER_M3U8_URL;
import static insty.constants.VideoConstants.DOMAIN;
import static insty.constants.VideoConstants.PATH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

import insty.cloudfront.adapter.CloudFrontSigner;
import insty.domain.video.dto.VideoHlsPlaylistReq;
import insty.domain.video.dto.VideoUploadReq;
import insty.domain.video.dto.VideoUploadRes;
import insty.domain.video.implement.VideoIssuer;
import insty.domain.video.implement.VideoValidator;
import insty.domain.video.implement.VideoWriter;
import insty.domain.video.repository.VideoAnswerRepository;
import insty.domain.video.repository.VideoCourseRepository;
import insty.domain.video.repository.VideoEncodingRepository;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

@Tag("integration")
@SpringBootTest
@Transactional
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
    @Autowired
    private VideoEncodingRepository videoEncodingRepository;

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

    @Sql(statements = {
            "INSERT INTO shared.video_courses (id, video_uuid, course_id, s3key, extension, original_file_name, thumbnail_url, encoding_status, encoding_at, analysis_status, analysis_at, created_at, updated_at, is_deleted) "
                    + "VALUES (1L, '00000000-0000-0000-0000-000000000001', 1, 'vod/COURSE/hls/00000000-0000-0000-0000-000000000001/fileName.mp4', 'mp4', 'fileName.mp4', NULL, 'PROCESSING', NOW(), 'WAITING', NULL, NOW(), NOW(), FALSE);",
            "INSERT INTO web_service.video_encodings (id, video_uuid, format, encoding_s3_key, created_at) " +
                    "VALUES (1L, '00000000-0000-0000-0000-000000000001', 'hls', 'vod/COURSE/hls/00000000-0000-0000-0000-000000000001/fileName', NOW())"
    })
    @Test
    void getSignedCookieMap_정상() {
        // given
        VideoType videoType = VideoType.COURSE;
        Long id = 1L;
        VideoHlsPlaylistReq req = new VideoHlsPlaylistReq(videoType, id);

        // mock
        Map<String, String> cookieMap = new HashMap<>();
        cookieMap.put("CloudFront-Signature", "sig-value");
        cookieMap.put("CloudFront-Key-Pair-Id", "key-pair-id");
        cookieMap.put("CloudFront-Policy", "policy-value");
        when(cloudFrontSigner.generateSignedCookiesForVideo(anyString(), anyString()))
                .thenReturn(cookieMap);

        when(cloudFrontSigner.generateResourcePath(anyString(), anyString()))
                .thenAnswer(invocation -> {
                    String domain = invocation.getArgument(0);
                    String path = invocation.getArgument(1);
                    return "https://" + domain + "/" + path;
                });

        when(appProperties.getDomain())
                .thenReturn("insty.test.com");

        // when
        Map<String, String> res = videoService.getSignedCookieMap(req);

        // then
        assertThat(res).isNotNull();
        assertThat(res.size()).isEqualTo(6);
        assertThat(cookieMap.get(CLOUDFRONT_KEY_PAIR_ID)).isNotNull();
        assertThat(cookieMap.get(CLOUDFRONT_SIGNATURE)).isNotNull();
        assertThat(cookieMap.get(CLOUDFRONT_POLICY)).isNotNull();
        assertThat(cookieMap.get(PATH)).isEqualTo("/vod/COURSE/hls/00000000-0000-0000-0000-000000000001/");
        assertThat(cookieMap.get(CLOUDFRONT_SIGNED_MASTER_M3U8_URL)).isEqualTo(
                "https://insty.test.com/vod/COURSE/hls/00000000-0000-0000-0000-000000000001/fileName.m3u8");
        assertThat(cookieMap.get(DOMAIN)).isEqualTo("insty.test.com");
    }
}