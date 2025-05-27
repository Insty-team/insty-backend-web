package insty.cloudfront.adapter;

import static insty.cloudfront.constant.CloudFrontConstants.CLOUDFRONT_KEY_PAIR_ID;
import static insty.cloudfront.constant.CloudFrontConstants.CLOUDFRONT_POLICY;
import static insty.cloudfront.constant.CloudFrontConstants.CLOUDFRONT_SIGNATURE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.amazonaws.services.cloudfront.CloudFrontCookieSigner;
import com.amazonaws.services.cloudfront.CloudFrontCookieSigner.CookiesForCustomPolicy;
import com.amazonaws.services.cloudfront.util.SignerUtils;
import java.security.PrivateKey;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class CloudFrontSignerTest {

    private CloudFrontSigner cloudFrontSigner;

    @BeforeEach
    void setUp() {
        cloudFrontSigner = new CloudFrontSigner("key-pair-id", "/private/key.pem");
    }

    @Test
    void generateSignedCookiesForVideo_정상() {
        // given
        String domain = "insty.test.com";
        String objectPath = "vod/COURSE/hls/00000000-0000-0000-0000-000000000001/*";

        // mock
        PrivateKey fakePrivateKey = mock(PrivateKey.class);
        CookiesForCustomPolicy fakeCookies = mock(CookiesForCustomPolicy.class);
        Map.Entry<String, String> signature = Map.entry("CloudFront-Signature", "sig-value");
        Map.Entry<String, String> keyPairId = Map.entry("CloudFront-Key-Pair-Id", "key-pair-id");
        Map.Entry<String, String> policy = Map.entry("CloudFront-Policy", "policy-value");

        try (MockedStatic<SignerUtils> signerUtilsMock = mockStatic(SignerUtils.class);
             MockedStatic<CloudFrontCookieSigner> cookieSignerMock = mockStatic(CloudFrontCookieSigner.class);
        ) {
            signerUtilsMock.when(() -> SignerUtils.loadPrivateKey(anyString()))
                    .thenReturn(fakePrivateKey);

            cookieSignerMock.when(() ->
                    CloudFrontCookieSigner.getCookiesForCustomPolicy(
                            anyString(), any(), anyString(), any(), isNull(), isNull())
            ).thenReturn(fakeCookies);
            when(fakeCookies.getSignature()).thenReturn(signature);
            when(fakeCookies.getKeyPairId()).thenReturn(keyPairId);
            when(fakeCookies.getPolicy()).thenReturn(policy);

            // when
            Map<String, String> cookieMap = cloudFrontSigner.generateSignedCookiesForVideo(domain, objectPath);

            // then
            assertThat(cookieMap.size()).isEqualTo(3);
            assertThat(cookieMap.get(CLOUDFRONT_KEY_PAIR_ID)).contains("key-pair-id");
            assertThat(cookieMap.get(CLOUDFRONT_SIGNATURE)).isNotNull();
            assertThat(cookieMap.get(CLOUDFRONT_POLICY)).isNotNull();
        }
    }
}