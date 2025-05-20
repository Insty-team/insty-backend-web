package insty.domain.video.implement;

import insty.domain.video.dto.VideoUploadRes;
import insty.s3.adapter.S3UrlIssuer;
import insty.s3.constant.S3Constants;
import insty.util.TimeUtils;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VideoIssuer {

    private final S3UrlIssuer s3UrlIssuer;

    public VideoUploadRes getUploadInfo(UUID uuid, String s3Key, String contentType) {
        String presignedUrl = s3UrlIssuer.generatePresignedUrl(s3Key, contentType);
        Instant expiredAt = TimeUtils.getMinutesLater(S3Constants.URL_EXPIRATION_MINUTES);
        return new VideoUploadRes(uuid, presignedUrl, expiredAt);
    }
}
