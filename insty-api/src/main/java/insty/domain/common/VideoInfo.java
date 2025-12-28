package insty.domain.common;

import insty.model.video.VideoAnswer;
import insty.model.video.VideoCommunityComment;
import insty.model.video.VideoCommunityPost;
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

    public static VideoInfo of(VideoCommunityPost videoCommunityPost) {
        if (videoCommunityPost == null) {
            return null;
        }
        return new VideoInfo(VideoType.COMMUNITY_POST, videoCommunityPost.getVideoUuid(),
                videoCommunityPost.getOriginalFileName());
    }

    public static VideoInfo of(VideoCommunityComment videoCommunityComment) {
        if (videoCommunityComment == null) {
            return null;
        }
        return new VideoInfo(VideoType.COMMUNITY_COMMENT, videoCommunityComment.getVideoUuid(),
                videoCommunityComment.getOriginalFileName());
    }
}
