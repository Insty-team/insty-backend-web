package insty.cloudfront.adapter;

import com.amazonaws.services.cloudfront.CloudFrontCookieSigner;
import com.amazonaws.services.cloudfront.CloudFrontCookieSigner.CookiesForCustomPolicy;
import com.amazonaws.services.cloudfront.CloudFrontUrlSigner;
import com.amazonaws.services.cloudfront.util.SignerUtils;
import com.amazonaws.services.cloudfront.util.SignerUtils.Protocol;
import insty.cloudfront.constant.CloudFrontConstants;
import insty.cloudfront.error.CloudFrontErrorCode;
import insty.exception.CustomException;
import java.security.PrivateKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CloudFrontSigner {

    private final String keyPairId;
    private final String privateKeyPath;

    public CloudFrontSigner(
            @Value("${aws.cloudfront.key-pair-id}") String keyPairId,
            @Value("${aws.cloudfront.private-key-path}") String privateKeyPath
    ) {
        this.keyPairId = keyPairId;
        this.privateKeyPath = privateKeyPath;
    }

    /**
     * CloudFront 유틸 클래스를 이용해 리소스에 접근할 수 있는 쿠키를 발급한다.
     *
     * @param domain     {도메인}
     * @param objectPath /vod/{type}/hls/{uuid}/*
     * @return CloudFront-Key-Pair-Id, CloudFront-Signature, CloudFront-Policy
     */
    public Map<String, String> generateSignedCookiesForVideo(String domain, String objectPath) {
        try {
            String resourcePath = generateResourcePath(domain, objectPath);
            PrivateKey privateKey = SignerUtils.loadPrivateKey(privateKeyPath);
            Instant expiredAt = Instant.now()
                    .plus(Duration.ofHours(CloudFrontConstants.GET_VIDEO_URL_EXPIRATION_HOURS));
            Date expiration = Date.from(expiredAt);

            CookiesForCustomPolicy cookies = CloudFrontCookieSigner.getCookiesForCustomPolicy(
                    resourcePath, privateKey, keyPairId, expiration, null, null);

            Map<String, String> cookieMap = new HashMap<>();
            cookieMap.put(cookies.getSignature().getKey(), cookies.getSignature().getValue());
            cookieMap.put(cookies.getKeyPairId().getKey(), cookies.getKeyPairId().getValue());
            cookieMap.put(cookies.getPolicy().getKey(), cookies.getPolicy().getValue());
            return cookieMap;
        } catch (Exception e) {
            log.error("CloudFront 에러\n", e);
            throw new CustomException(CloudFrontErrorCode.CLOUD_FRONT_GENERATE_SIGNED_COOKIE_FAIL);
        }
    }

    public String generatePresignedUrlForVideo(String domain, String objectPath) {
        try {
            String resourcePath = generateResourcePath(domain, objectPath);
            PrivateKey privateKey = SignerUtils.loadPrivateKey(privateKeyPath);
            Instant expiredAt = Instant.now()
                    .plus(Duration.ofMinutes(CloudFrontConstants.GET_PREVIEW_VIDEO_URL_EXPIRATION_MINUTES));
            Date expiration = Date.from(expiredAt);

            return CloudFrontUrlSigner.getSignedURLWithCannedPolicy(
                    resourcePath, keyPairId, privateKey, expiration
            );
        } catch (Exception e) {
            log.error("CloudFront 에러\n", e);
            throw new CustomException(CloudFrontErrorCode.CLOUD_FRONT_GENERATE_PRESIGNED_URL_FAIL);
        }
    }

    /**
     * 도메인과 리소스 경로로부터 완전한 https url을 만든다.
     *
     * @param domain       {도메인}
     * @param resourcePath /vod/{type}/hls/{uuid}
     * @return https://{도메인}/vod/{type}/hls/{uuid}
     */
    public String generateResourcePath(String domain, String resourcePath) {
        return Protocol.https + "://" + domain + "/" + resourcePath;
    }
}
