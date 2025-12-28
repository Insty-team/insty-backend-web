package insty.domain.video.strategy.videoAnswer;

import insty.domain.video.repository.VideoAnswerRepository;
import insty.domain.video.strategy.VideoValidateStrategy;
import insty.error.VideoErrorCode;
import insty.exception.CustomException;
import insty.global.property.VideoUploadLimitProperties;
import insty.model.video.EncodingStatus;
import insty.model.video.VideoAnswer;
import insty.util.DateUtils;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class VideoAnswerValidateStrategy implements VideoValidateStrategy {

    private final VideoUploadLimitProperties videoUploadLimitProperties;

    private final VideoAnswerRepository videoAnswerRepository;

    @Override
    public void validateUploadable(Long userId) {
        Instant startOfToday = DateUtils.getStartOfTodayInKorea();

        int durationSum = videoAnswerRepository.findEncodingDuration(userId, startOfToday,
                        EncodingStatus.getExceedUploadLimitTarget())
                .stream()
                .mapToInt(Integer::intValue)
                .sum();
        if (durationSum >= videoUploadLimitProperties.getAnswer() * 60) {
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
        VideoAnswer videoAnswer = videoAnswerRepository.findByCourseAnswerIdAndIsDeleted(parentId, false)
                .orElseThrow(() -> new CustomException(VideoErrorCode.VIDEO_NOT_FOUND));
        if (videoAnswer.getEncodingStatus() == EncodingStatus.FAILED) {
            throw new CustomException(VideoErrorCode.VIDEO_ENCODING_FAILED);
        }
        if (videoAnswer.getEncodingStatus() == EncodingStatus.FAILED_INVALID_VIDEO_LENGTH) {
            throw new CustomException(VideoErrorCode.VIDEO_ENCODING_FAILED_INVALID_LENGTH);
        }
        if (videoAnswer.getEncodingStatus() == EncodingStatus.FAILED_NOT_FOUND_VOICE) {
            throw new CustomException(VideoErrorCode.VIDEO_ENCODING_FAILED_NOT_FOUND_VOICE);
        }
        if (videoAnswer.getEncodingStatus() != EncodingStatus.COMPLETED) {
            throw new CustomException(VideoErrorCode.VIDEO_NOT_FINISHED_ENCODING);
        }
    }
}
