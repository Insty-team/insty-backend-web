package insty.cloudfront.adapter;

import com.amazonaws.services.cloudfront.CloudFrontUrlSigner;
import com.amazonaws.services.cloudfront.util.SignerUtils;
import insty.cloudfront.constant.CloudFrontConstants;
import insty.cloudfront.error.CloudFrontErrorCode;
import insty.exception.CustomException;
import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CloudFrontSigner {

    @Value("${aws.cloudfront.domain}")
    private String domain;

    @Value("${aws.cloudfront.key-pair-id}")
    private String keyPairId;

    @Value("${aws.cloudfront.private-key-path}")
    private String privateKeyPath;

    public String generateSignedUrlForVideo(String objectKey) {
        try {
            Instant expiredAt = Instant.now()
                    .plus(Duration.ofHours(CloudFrontConstants.GET_VIDEO_URL_EXPIRATION_HOURS));
            Date expiration = Date.from(expiredAt);

            File privateKeyFile = new File(privateKeyPath);

            return CloudFrontUrlSigner.getSignedURLWithCannedPolicy(
                    SignerUtils.Protocol.https,
                    domain,
                    privateKeyFile,
                    objectKey,
                    keyPairId,
                    expiration
            );
        } catch (Exception e) {
            throw new CustomException(CloudFrontErrorCode.CLOUD_FRONT_GENERATE_SIGNED_URL_FAIL);
        }
    }
}
