package insty.domain.common;

import insty.model.video.VideoAnswer;
import insty.model.video.VideoCourse;
import insty.model.video.VideoQuestion;
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

    public static VideoInfo of(VideoQuestion videoQuestion) {
        if (videoQuestion == null) {
            return null;
        }
        return new VideoInfo(VideoType.QUESTION, videoQuestion.getVideoUuid(), videoQuestion.getOriginalFileName());
    }

    public static VideoInfo of(VideoAnswer videoAnswer) {
        if (videoAnswer == null) {
            return null;
        }
        return new VideoInfo(VideoType.ANSWER, videoAnswer.getVideoUuid(), videoAnswer.getOriginalFileName());
    }
}
