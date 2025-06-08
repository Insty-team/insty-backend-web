package insty.domain.video.implement;

import static insty.cloudfront.constant.CloudFrontConstants.CLOUDFRONT_SIGNED_MASTER_M3U8_URL;
import static insty.constants.VideoConstants.DOMAIN;
import static insty.constants.VideoConstants.PATH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import insty.cloudfront.adapter.CloudFrontSigner;
import insty.global.property.AppProperties;
import insty.s3.adapter.S3UrlIssuer;
import insty.s3.constant.S3Constants;
import insty.s3.dto.PresignedUrlDto;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class VideoAccessManagerTest {

    @InjectMocks
    private VideoAccessManager videoAccessManager;

    @Mock
    private S3UrlIssuer s3UrlIssuer;
    @Mock
    private CloudFrontSigner cloudFrontSigner;
    @Mock
    private AppProperties appProperties;

    @Test
    void getUploadInfo_정상() {
        // given
        String s3Key = "vod/COURSE/mp4/uuid/fileName.mp4";
        String contentType = "video/mp4";

        // mock
        when(s3UrlIssuer.generatePresignedUrl(s3Key, contentType))
                .thenReturn(new PresignedUrlDto("https://s3.ap-northeast-2.amazonaws.com/bucket/key?..",
                        Instant.now().plus(Duration.ofMinutes(S3Constants.UPLOAD_URL_EXPIRATION_MINUTES))));

        // when
        PresignedUrlDto uploadInfo = videoAccessManager.getUploadInfo(s3Key, contentType);

        // then
        assertThat(uploadInfo).isNotNull();
    }

    @Test
    void getSignedCookieMap_정상() {
        // given
        String encodingVideoDirectoryPath = "vod/COURSE/hls/00000000-0000-0000-0000-000000000001";
        String hlsMasterFileKey = "vod/COURSE/hls/00000000-0000-0000-0000-000000000001/fileName.m3u8";

        // mock
        when(appProperties.getDomain())
                .thenReturn("insty.test.com");
        Map<String, String> signedCookieMap = new HashMap<>(Map.of(
                "CloudFront-Signature", "sig-value",
                "CloudFront-Key-Pair-Id", "key-pair-id",
                "CloudFront-Policy", "policy-value"
        ));
        when(cloudFrontSigner.generateSignedCookiesForVideo(anyString(), anyString()))
                .thenReturn(signedCookieMap);
        when(cloudFrontSigner.generateResourcePath(anyString(), anyString()))
                .thenAnswer(invocation -> {
                    String domain = invocation.getArgument(0);
                    String resourcePath = invocation.getArgument(1);
                    return "https://" + domain + "/" + resourcePath;
                });

        // when
        Map<String, String> result = videoAccessManager.getSignedCookieMap(encodingVideoDirectoryPath,
                hlsMasterFileKey);

        // then
        assertThat(result.size()).isEqualTo(6);
        assertThat(result.get(PATH)).isEqualTo("/vod/COURSE/hls/00000000-0000-0000-0000-000000000001/");
        assertThat(result.get(CLOUDFRONT_SIGNED_MASTER_M3U8_URL)).isEqualTo(
                "https://insty.test.com/vod/COURSE/hls/00000000-0000-0000-0000-000000000001/fileName.m3u8");
        assertThat(result.get(DOMAIN)).isEqualTo("insty.test.com");
    }

    @Test
    void getPresignedUrl_정상() {
        // given
        String encodingVideoKey = "vod/COURSE/hls/00000000-0000-0000-0000-000000000001/fileName";

        // mock
        when(appProperties.getDomain())
                .thenReturn("insty.test.com");
        when(cloudFrontSigner.generatePresignedUrlForVideo(anyString(), anyString()))
                .thenReturn("pre-signed video url");

        // when
        String presignedUrl = videoAccessManager.getPresignedUrl(encodingVideoKey);

        // then
        assertThat(presignedUrl).isNotNull();
    }
}