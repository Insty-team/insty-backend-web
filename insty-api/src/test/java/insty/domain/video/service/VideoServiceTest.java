package insty.domain.video.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

import insty.domain.video.dto.VideoUploadReq;
import insty.domain.video.dto.VideoUploadRes;
import insty.domain.video.implement.VideoIssuer;
import insty.domain.video.implement.VideoValidator;
import insty.domain.video.implement.VideoWriter;
import insty.domain.video.repository.VideoCourseRepository;
import insty.s3.adapter.S3UrlIssuer;
import insty.s3.constant.S3Constants;
import insty.s3.dto.PresignedUrlDto;
import java.time.Duration;
import java.time.Instant;
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
    @Autowired
    private VideoIssuer videoIssuer;
    @Autowired
    private VideoCourseRepository videoCourseRepository;
    @MockitoBean
    private S3UrlIssuer s3UrlIssuer;

    @Test
    void getPreSignedURLForUpload_정상() {
        // given
        String fileName = "fileName.mp4";
        String contentType = "video/mp4";
        VideoUploadReq req = new VideoUploadReq(fileName, contentType);

        // mock
        Instant expiredAt = Instant.now().plus(Duration.ofMinutes(S3Constants.URL_EXPIRATION_MINUTES));
        when(s3UrlIssuer.generatePresignedUrl(anyString(), anyString()))
                .thenReturn(new PresignedUrlDto(
                        "https://s3.ap-northeast-2.amazonaws.com/bucket/key?..",
                        expiredAt
                ));

        // when
        VideoUploadRes res = videoService.getPreSignedURLForUpload(req);

        // then
        assertThat(res).isNotNull();
        assertThat(res.uuid()).isNotNull();
        assertThat(res.uploadUrl()).isNotNull();
        assertThat(res.expiredAt()).isEqualTo(expiredAt);
    }
}