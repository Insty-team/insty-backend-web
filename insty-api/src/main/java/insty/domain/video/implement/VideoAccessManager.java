package insty.domain.video.implement;

import static insty.cloudfront.constant.CloudFrontConstants.CLOUDFRONT_SIGNED_MASTER_M3U8_URL;
import static insty.constants.VideoConstants.DOMAIN;
import static insty.constants.VideoConstants.PATH;

import insty.cloudfront.adapter.CloudFrontSigner;
import insty.global.property.AppProperties;
import insty.s3.adapter.S3UrlIssuer;
import insty.s3.dto.PresignedUrlDto;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VideoAccessManager {

    private final S3UrlIssuer s3UrlIssuer;
    private final CloudFrontSigner cloudFrontSigner;
    private final AppProperties appProperties;

    public PresignedUrlDto getUploadInfo(String s3Key, String contentType) {
        return s3UrlIssuer.generatePresignedUrl(s3Key, contentType);
    }

    /**
     * CloudFront의 Signed Cookie와 여러 정보를 담은 Map을 반환한다.
     *
     * @param encodingVideoDirectoryPath vod/{type}/hls/{uuid}
     * @param hlsMasterFileKey           vod/{type}/hls/{uuid}/fileName.m3u8
     * @param expiredMinutes             만료기간(분)
     * @return <br>Path : 쿠키를 적용할 api path
     * <br>CloudFront-Signed-Url : 클라이언트가 접근할 HLS 마스터 파일 경로
     * <br>Domain : 쿠키를 적용할 도메인
     */
    public Map<String, String> getSignedCookieMap(String encodingVideoDirectoryPath, String hlsMasterFileKey,
                                                  long expiredMinutes) {
        Map<String, String> signedCookieMap = cloudFrontSigner.generateSignedCookiesForVideo(
                appProperties.getDomain(), encodingVideoDirectoryPath + "/*", expiredMinutes);
        signedCookieMap.put(PATH, "/" + encodingVideoDirectoryPath + "/");
        signedCookieMap.put(CLOUDFRONT_SIGNED_MASTER_M3U8_URL,
                cloudFrontSigner.generateResourcePath(appProperties.getDomain(), hlsMasterFileKey));
        signedCookieMap.put(DOMAIN, appProperties.getDomain());
        return signedCookieMap;
    }
}
