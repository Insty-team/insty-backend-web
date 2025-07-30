package insty.domain.common;

import insty.model.video.VideoCourse;
import insty.model.video.VideoType;
import java.util.UUID;

public record VideoInfo(
        VideoType videoType,
        UUID videoUuid,
        String originFileName
) {

    public static VideoInfo of(VideoCourse videoCourse) {
        if (videoCourse == null) {
            return null;
        }
        return new VideoInfo(VideoType.COURSE, videoCourse.getVideoUuid(), videoCourse.getOriginalFileName());
    }
}
