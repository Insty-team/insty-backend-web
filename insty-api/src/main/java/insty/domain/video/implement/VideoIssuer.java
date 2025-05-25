package insty.domain.video.implement;

import static insty.cloudfront.constant.CloudFrontConstants.CLOUDFRONT_SIGNED_URL;
import static insty.constants.VideoConstants.HLS_MASTER_FILE;

import insty.cloudfront.adapter.CloudFrontSigner;
import insty.s3.adapter.S3UrlIssuer;
import insty.s3.dto.PresignedUrlDto;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VideoIssuer {

    private final S3UrlIssuer s3UrlIssuer;
    private final CloudFrontSigner cloudFrontSigner;

    public PresignedUrlDto getUploadInfo(String s3Key, String contentType) {
        return s3UrlIssuer.generatePresignedUrl(s3Key, contentType);
    }

    public Map<String, String> getSignedCookieMap(String encodingVideoDirectoryPath, String hlsMasterFileKey) {
        Map<String, String> signedCookieMap = cloudFrontSigner.generateSignedCookiesForVideo(
                encodingVideoDirectoryPath);
        signedCookieMap.put(CLOUDFRONT_SIGNED_URL, cloudFrontSigner.generateResourcePath(encodingVideoDirectoryPath));
        signedCookieMap.put(HLS_MASTER_FILE, cloudFrontSigner.generateResourcePath(hlsMasterFileKey));
        return signedCookieMap;
    }
}
