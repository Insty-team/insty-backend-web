package insty.domain.video.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import insty.s3.adapter.S3UrlIssuer;
import insty.s3.constant.S3Constants;
import insty.s3.dto.PresignedUrlDto;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class VideoIssuerTest {

    @Mock
    private S3UrlIssuer s3UrlIssuer;

    @InjectMocks
    private VideoIssuer videoIssuer;

    @Test
    void getUploadInfo_정상() {
        // given
        String s3Key = "vod/COURSE/mp4/uuid/fileName.mp4";
        String contentType = "video/mp4";

        // mock
        when(s3UrlIssuer.generatePresignedUrl(s3Key, contentType))
                .thenReturn(new PresignedUrlDto("https://s3.ap-northeast-2.amazonaws.com/bucket/key?..",
                        Instant.now().plus(Duration.ofMinutes(S3Constants.URL_EXPIRATION_MINUTES))));

        // when
        PresignedUrlDto uploadInfo = videoIssuer.getUploadInfo(s3Key, contentType);

        // then
        assertThat(uploadInfo).isNotNull();
    }
}