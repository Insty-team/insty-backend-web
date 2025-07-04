package insty.domain.video.implement;

import insty.error.VideoErrorCode;
import insty.exception.CustomException;
import insty.global.property.AppProperties;
import insty.s3.adapter.S3FileManager;
import insty.util.VideoUtils;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VideoFileReader {

    private final S3FileManager s3FileManager;

    private final AppProperties appProperties;

    public String getThumbnailUrl(UUID videoUuid) {
        String thumbnailKey = VideoUtils.getVideoBaseThumbnailKey(videoUuid);
        if (!s3FileManager.doesFileExist(thumbnailKey)) {
            throw new CustomException(VideoErrorCode.VIDEO_BASIC_THUMBNAIL_NOT_FOUND);
        }
        return VideoUtils.getVideoBaseThumbnailUrl(appProperties.getDomain(), videoUuid);
    }
}
