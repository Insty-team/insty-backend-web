package insty.domain.video.strategy.videoQuestion;

import insty.domain.video.repository.VideoQuestionRepository;
import insty.domain.video.strategy.VideoValidateStrategy;
import insty.error.VideoErrorCode;
import insty.exception.CustomException;
import insty.global.property.VideoUploadLimitProperties;
import insty.model.video.EncodingStatus;
import insty.model.video.VideoQuestion;
import insty.util.DateUtils;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class VideoQuestionValidateStrategy implements VideoValidateStrategy {

    private final VideoUploadLimitProperties videoUploadLimitProperties;

    private final VideoQuestionRepository videoQuestionRepository;

    @Override
    public void validateUploadable(Long userId) {
        Instant startOfToday = DateUtils.getStartOfTodayInKorea();

        int durationSum = videoQuestionRepository.findEncodingDuration(userId, startOfToday,
                        EncodingStatus.getExceedUploadLimitTarget())
                .stream()
                .mapToInt(Integer::intValue)
                .sum();
        if (durationSum >= videoUploadLimitProperties.getQuestion() * 60) {
            throw new CustomException(VideoErrorCode.VIDEO_EXCEED_UPLOAD_LIMIT);
        }
    }

    @Override
    public void validateReadable(Long userId, Long videoId) {
        // TODO: 필요하다면 검증 로직 작성
        return;
    }

    @Override
    public void verifyEncodingCompletedAndDeleted(Long parentId) {
        VideoQuestion videoQuestion = videoQuestionRepository.findByCommunityQuestionIdAndIsDeleted(parentId, false)
                .orElseThrow(() -> new CustomException(VideoErrorCode.VIDEO_NOT_FOUND));
        if (videoQuestion.getEncodingStatus() == EncodingStatus.FAILED) {
            throw new CustomException(VideoErrorCode.VIDEO_ENCODING_FAILED);
        }
        if (videoQuestion.getEncodingStatus() == EncodingStatus.FAILED_INVALID_VIDEO_LENGTH) {
            throw new CustomException(VideoErrorCode.VIDEO_ENCODING_FAILED_INVALID_LENGTH);
        }
        if (videoQuestion.getEncodingStatus() == EncodingStatus.FAILED_NOT_FOUND_VOICE) {
            throw new CustomException(VideoErrorCode.VIDEO_ENCODING_FAILED_NOT_FOUND_VOICE);
        }
        if (videoQuestion.getEncodingStatus() != EncodingStatus.COMPLETED) {
            throw new CustomException(VideoErrorCode.VIDEO_NOT_FINISHED_ENCODING);
        }
    }
}
