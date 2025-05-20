package insty.domain.video.implement;

import insty.domain.video.dto.VideoUploadRes;
import insty.error.VideoErrorCode;
import insty.exception.CustomException;
import insty.s3.adapter.S3UrlIssuer;
import insty.s3.constant.S3Constants;
import insty.util.FileUtils;
import insty.util.TimeUtils;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VideoIssuer {

    private final S3UrlIssuer s3UrlIssuer;

    public VideoUploadRes getUploadInfo(UUID uuid, String fileName, String contentType) {
        String extension = FileUtils.extractExtension(fileName)
                .orElseThrow(() -> new CustomException(VideoErrorCode.VIDEO_INVALID_FILE_NAME));
        String key = "vod/" + extension + "/" + uuid + "_" + fileName;

        String presignedUrl = s3UrlIssuer.generatePresignedUrl(key, contentType);
        Instant expiredAt = TimeUtils.getMinutesLater(S3Constants.URL_EXPIRATION_MINUTES);
        return new VideoUploadRes(uuid, presignedUrl, expiredAt);
    }
}
