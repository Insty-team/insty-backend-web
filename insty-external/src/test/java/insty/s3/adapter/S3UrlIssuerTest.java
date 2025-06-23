package insty.s3.adapter;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import insty.s3.constant.S3Constants;
import java.net.MalformedURLException;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class S3UrlIssuerTest {

    @Mock
    private S3Presigner s3Presigner;

    @InjectMocks
    private S3UrlIssuer s3UrlIssuer;

    @BeforeEach
    void setUp() {
        s3UrlIssuer = new S3UrlIssuer(s3Presigner, "test-bucket");
    }

    @Test
    void generatePresignedUrl_정상() throws MalformedURLException {
        // given
        String key = "vod/COURSE/mp4/uuid/fileName.mp4";
        String contentType = "video/mp4";

        // mock
        String fakeUrl = "https://s3.ap-northeast-2.amazonaws.com/bucket/key?..";
        Instant expiration = Instant.now().plus(Duration.ofMinutes(S3Constants.UPLOAD_URL_EXPIRATION_MINUTES));
        PresignedPutObjectRequest mockPresigned = mock(PresignedPutObjectRequest.class);

        when(mockPresigned.url()).thenReturn(URI.create(fakeUrl).toURL());
        when(mockPresigned.expiration()).thenReturn(expiration);
        when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class)))
                .thenReturn(mockPresigned);

        // when
        s3UrlIssuer.generatePresignedUrl(key, contentType);
    }
}