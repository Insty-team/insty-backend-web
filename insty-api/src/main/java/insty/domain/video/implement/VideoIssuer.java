package insty.domain.video.implement;

import static insty.cloudfront.constant.CloudFrontConstants.CLOUDFRONT_SIGNED_URL;
import static insty.constants.VideoConstants.HLS_MASTER_FILE;

import insty.cloudfront.adapter.CloudFrontSigner;
import insty.s3.adapter.S3EncodingVideoReader;
import insty.s3.adapter.S3UrlIssuer;
import insty.s3.dto.PresignedUrlDto;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VideoIssuer {

    private final S3UrlIssuer s3UrlIssuer;
    private final S3EncodingVideoReader s3EncodingVideoReader;
    private final CloudFrontSigner cloudFrontSigner;

    public PresignedUrlDto getUploadInfo(String s3Key, String contentType) {
        return s3UrlIssuer.generatePresignedUrl(s3Key, contentType);
    }

    public String getSignedM3u8Url(String encodingS3Key) {
        String m3u8Content = s3EncodingVideoReader.loadM3u8Content(encodingS3Key + ".m3u8");
        return signM3u8Content(m3u8Content);
    }

    private String signM3u8Content(String m3u8Content) {
        return Arrays.stream(m3u8Content.split("\n"))
                .map(this::replaceIfTs)
                .collect(Collectors.joining("\n"));
    }

    private String replaceIfTs(String line) {
        if (line.trim().endsWith(".ts")) {
            return cloudFrontSigner.generateSignedUrlForVideo(line.trim());
        }
        return line;
    }

    public Map<String, String> getSignedCookieMap(String encodingVideoDirectoryPath, String hlsMasterFileKey) {
        Map<String, String> signedCookieMap = cloudFrontSigner.generateSignedCookiesForVideo(
                encodingVideoDirectoryPath);
        signedCookieMap.put(CLOUDFRONT_SIGNED_URL, cloudFrontSigner.generateResourcePath(encodingVideoDirectoryPath));
        signedCookieMap.put(HLS_MASTER_FILE, cloudFrontSigner.generateResourcePath(hlsMasterFileKey));
        return signedCookieMap;
    }
}
