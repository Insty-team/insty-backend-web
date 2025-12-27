package insty.domain.video.strategy;

import insty.domain.video.strategy.videoAnswer.VideoAnswerReadStrategy;
import insty.domain.video.strategy.videoAnswer.VideoAnswerValidateStrategy;
import insty.domain.video.strategy.videoAnswer.VideoAnswerWriteStrategy;
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

    private final VideoCourseValidateStrategy videoCourseValidateStrategy;
    private final VideoQuestionValidateStrategy videoQuestionValidateStrategy;
    private final VideoAnswerValidateStrategy videoAnswerValidateStrategy;

    private final VideoCourseWriteStrategy videoCourseWriteStrategy;
    private final VideoQuestionWriteStrategy videoQuestionWriteStrategy;
    private final VideoAnswerWriteStrategy videoAnswerWriteStrategy;

    public VideoReadStrategy getReadStrategy(VideoType videoType) {
        return switch (videoType) {
            case COURSE -> videoCourseReadStrategy;
            case QUESTION -> videoQuestionReadStrategy;
            case ANSWER -> videoAnswerReadStrategy;
            default -> throw new IllegalArgumentException("Unsupported video type: " + videoType);
        };
    }

    public VideoValidateStrategy getValidateStrategy(VideoType videoType) {
        return switch (videoType) {
            case COURSE -> videoCourseValidateStrategy;
            case QUESTION -> videoQuestionValidateStrategy;
            case ANSWER -> videoAnswerValidateStrategy;
            default -> throw new IllegalArgumentException("Unsupported video type: " + videoType);
        };
    }

    public VideoWriteStrategy getWriteStrategy(VideoType videoType) {
        return switch (videoType) {
            case COURSE -> videoCourseWriteStrategy;
            case QUESTION -> videoQuestionWriteStrategy;
            case ANSWER -> videoAnswerWriteStrategy;
            default -> throw new IllegalArgumentException("Unsupported video type: " + videoType);
        };
    }
}
