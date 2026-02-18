package insty.domain.video.strategy;

import insty.domain.video.strategy.videoAnswer.VideoAnswerReadStrategy;
import insty.domain.video.strategy.videoAnswer.VideoAnswerValidateStrategy;
import insty.domain.video.strategy.videoAnswer.VideoAnswerWriteStrategy;
import insty.domain.video.strategy.videoCommunityComment.VideoCommunityCommentReadStrategy;
import insty.domain.video.strategy.videoCommunityComment.VideoCommunityCommentValidateStrategy;
import insty.domain.video.strategy.videoCommunityComment.VideoCommunityCommentWriteStrategy;
import insty.domain.video.strategy.videoCommunityPost.VideoCommunityPostReadStrategy;
import insty.domain.video.strategy.videoCommunityPost.VideoCommunityPostValidateStrategy;
import insty.domain.video.strategy.videoCommunityPost.VideoCommunityPostWriteStrategy;
import insty.domain.video.strategy.videoCourse.VideoCourseReadStrategy;
import insty.domain.video.strategy.videoCourse.VideoCourseValidateStrategy;
import insty.domain.video.strategy.videoCourse.VideoCourseWriteStrategy;
import insty.domain.video.strategy.videoQuestion.VideoQuestionReadStrategy;
import insty.domain.video.strategy.videoQuestion.VideoQuestionValidateStrategy;
import insty.domain.video.strategy.videoQuestion.VideoQuestionWriteStrategy;
import insty.model.video.VideoType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VideoStrategyFactory {

    private final VideoCourseReadStrategy videoCourseReadStrategy;
    private final VideoQuestionReadStrategy videoQuestionReadStrategy;
    private final VideoAnswerReadStrategy videoAnswerReadStrategy;
    private final VideoCommunityPostReadStrategy videoCommunityPostReadStrategy;
    private final VideoCommunityCommentReadStrategy videoCommunityCommentReadStrategy;

    private final VideoCourseValidateStrategy videoCourseValidateStrategy;
    private final VideoQuestionValidateStrategy videoQuestionValidateStrategy;
    private final VideoAnswerValidateStrategy videoAnswerValidateStrategy;
    private final VideoCommunityPostValidateStrategy videoCommunityPostValidateStrategy;
    private final VideoCommunityCommentValidateStrategy videoCommunityCommentValidateStrategy;

    private final VideoCourseWriteStrategy videoCourseWriteStrategy;
    private final VideoQuestionWriteStrategy videoQuestionWriteStrategy;
    private final VideoAnswerWriteStrategy videoAnswerWriteStrategy;
    private final VideoCommunityPostWriteStrategy videoCommunityPostWriteStrategy;
    private final VideoCommunityCommentWriteStrategy videoCommunityCommentWriteStrategy;

    public VideoReadStrategy getReadStrategy(VideoType videoType) {
        return switch (videoType) {
            case COURSE -> videoCourseReadStrategy;
            case QUESTION -> videoQuestionReadStrategy;
            case ANSWER -> videoAnswerReadStrategy;
            case COMMUNITY_POST -> videoCommunityPostReadStrategy;
            case COMMUNITY_COMMENT -> videoCommunityCommentReadStrategy;
            default -> throw new IllegalArgumentException("Unsupported video type: " + videoType);
        };
    }

    public VideoValidateStrategy getValidateStrategy(VideoType videoType) {
        return switch (videoType) {
            case COURSE -> videoCourseValidateStrategy;
            case QUESTION -> videoQuestionValidateStrategy;
            case ANSWER -> videoAnswerValidateStrategy;
            case COMMUNITY_POST -> videoCommunityPostValidateStrategy;
            case COMMUNITY_COMMENT -> videoCommunityCommentValidateStrategy;
            default -> throw new IllegalArgumentException("Unsupported video type: " + videoType);
        };
    }

    public VideoWriteStrategy getWriteStrategy(VideoType videoType) {
        return switch (videoType) {
            case COURSE -> videoCourseWriteStrategy;
            case QUESTION -> videoQuestionWriteStrategy;
            case ANSWER -> videoAnswerWriteStrategy;
            case COMMUNITY_POST -> videoCommunityPostWriteStrategy;
            case COMMUNITY_COMMENT -> videoCommunityCommentWriteStrategy;
            default -> throw new IllegalArgumentException("Unsupported video type: " + videoType);
        };
    }
}
