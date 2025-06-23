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
import insty.domain.user.implement.UserReader;
import insty.domain.user.repository.UserRepository;
import insty.domain.video.dto.VideoHlsPlaylistReq;
import insty.domain.video.dto.VideoHlsPlaylistRes;
import insty.domain.video.dto.VideoUploadReq;
import insty.domain.video.dto.VideoUploadRes;
import insty.domain.video.implement.VideoAccessManager;
import insty.domain.video.implement.VideoValidator;
import insty.domain.video.implement.VideoWriter;
import insty.domain.video.repository.VideoAnswerRepository;
import insty.domain.video.repository.VideoCourseRepository;
import insty.domain.video.repository.VideoEncodingRepository;
import insty.global.property.AppProperties;
import insty.model.user.User;
import insty.model.user.UserFixture;
import insty.model.video.AnalysisStatus;
import insty.model.video.EncodingStatus;
import insty.model.video.VideoAnswer;
import insty.model.video.VideoCourse;
import insty.model.video.VideoType;
import insty.s3.adapter.S3FileManager;
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
    @Autowired
    private VideoAccessManager videoAccessManager;
    @Autowired
    private UserReader userReader;
    @Autowired
    private VideoCourseRepository videoCourseRepository;
    @Autowired
    private VideoAnswerRepository videoAnswerRepository;
    @Autowired
    private VideoEncodingRepository videoEncodingRepository;
    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private UuidProvider uuidProvider;
    @MockitoBean
    private S3UrlIssuer s3UrlIssuer;
    @MockitoBean
    private S3FileManager s3FileManager;
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
        User user = UserFixture.getUser();
        user = userRepository.save(user);

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
        VideoUploadRes res = videoService.getPreSignedURLForCourseVideoUpload(user.getId(), req);

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
        User user = UserFixture.getUser();
        user = userRepository.save(user);

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
        VideoUploadRes res = videoService.getPreSignedURLForAnswerVideoUpload(user.getId(), req);

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
        assertThat(videoAnswer.getEncodingStatus()).isEqualTo(EncodingStatus.PROCESSING);
        assertThat(videoAnswer.getEncodingAt()).isNotNull();
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) "
                    + "VALUES (1, 'example@example.com', 'example', 1234, null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, thumbnail_id, is_show, created_at, updated_at, is_deleted) "
                    + "VALUES (1, 1, '파이썬 설치 강의', '설명', 20000, 0, 0, '파이썬 개발 환경 설치가 처음인 초보자', null, true, NOW(), NOW(), false);",
            "INSERT INTO web_service.video_courses (id, video_uuid, course_id, user_id, s3key, extension, original_file_name, duration, encoding_status, encoding_at, analysis_status, analysis_at, created_at, updated_at, is_deleted) "
                    + "VALUES (1, '00000000-0000-0000-0000-000000000001', 1, 1, 'vod/COURSE/hls/00000000-0000-0000-0000-000000000001/fileName.mp4', 'mp4', 'fileName.mp4', 10, 'COMPLETED', NOW(), 'WAITING', NULL, NOW(), NOW(), FALSE);",
            "INSERT INTO web_service.video_encodings (id, video_uuid, format, encoding_s3_key, created_at) " +
                    "VALUES (1, '00000000-0000-0000-0000-000000000001', 'hls', 'vod/COURSE/hls/00000000-0000-0000-0000-000000000001/fileName', NOW())"
    })
    @Test
    void getSignedCookieMap_정상() {
        // given
        Long userId = 1L;
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
        Map<String, String> res = videoService.getSignedCookieMap(userId, req);

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

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) "
                    + "VALUES (1, 'example@example.com', 'example', 1234, null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, thumbnail_id, is_show, created_at, updated_at, is_deleted) "
                    + "VALUES (1, 1, '파이썬 설치 강의', '설명', 20000, 0, 0, '파이썬 개발 환경 설치가 처음인 초보자', null, true, NOW(), NOW(), false);",
            "INSERT INTO web_service.video_courses (id, video_uuid, course_id, user_id, s3key, extension, original_file_name, duration, encoding_status, encoding_at, analysis_status, analysis_at, created_at, updated_at, is_deleted) "
                    + "VALUES (1, '00000000-0000-0000-0000-000000000001', 1, 1, 'vod/COURSE/hls/00000000-0000-0000-0000-000000000001/fileName.mp4', 'mp4', 'fileName.mp4', 10, 'COMPLETED', NOW(), 'WAITING', NULL, NOW(), NOW(), FALSE);",
            "INSERT INTO web_service.video_encodings (id, video_uuid, format, encoding_s3_key, created_at) " +
                    "VALUES (1, '00000000-0000-0000-0000-000000000001', 'hls', 'vod/COURSE/hls/00000000-0000-0000-0000-000000000001/fileName', NOW())"
    })
    @Test
    void getPreviewVideo_정상() {
        // given
        VideoType videoType = VideoType.COURSE;
        Long id = 1L;
        VideoHlsPlaylistReq req = new VideoHlsPlaylistReq(videoType, id);

        // mock
        when(appProperties.getDomain())
                .thenReturn("insty.test.com");
        when(cloudFrontSigner.generatePresignedUrlForVideo(anyString(), anyString()))
                .thenReturn("pre-signed video url");

        // when
        VideoHlsPlaylistRes res = videoService.getPreviewVideo(req);

        // then
        assertThat(res).isNotNull();
        assertThat(res.signedUrl()).isNotNull();
    }
}