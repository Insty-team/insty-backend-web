package insty.domain.common;

import insty.model.video.VideoType;
import java.util.UUID;

public record VideoInfo(
        VideoType videoType,
        UUID videoUuid
) {

    public static VideoInfo of(VideoType videoType, UUID videoUuid) {
        return new VideoInfo(videoType, videoUuid);
    }
}
