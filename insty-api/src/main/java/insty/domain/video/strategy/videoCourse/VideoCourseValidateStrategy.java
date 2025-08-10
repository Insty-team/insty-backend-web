package insty.domain.video.strategy.videoCourse;

import static insty.constants.VideoConstants.VIDEO_COURSE_UPLOAD_MINUTES_LIMIT;

import insty.domain.video.repository.VideoCourseRepository;
import insty.domain.video.strategy.VideoValidateStrategy;
import insty.error.VideoErrorCode;
import insty.exception.CustomException;
import insty.model.video.EncodingStatus;
import insty.model.video.VideoCourse;
import insty.util.DateUtils;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class VideoCourseValidateStrategy implements VideoValidateStrategy {

    private final VideoCourseRepository videoCourseRepository;

    @Override
    public void validateUploadable(Long userId) {
        Instant startOfToday = DateUtils.getStartOfTodayInKorea();

        int durationSum = videoCourseRepository.findEncodingDurationByUserIdAndEncodingAtGreaterThan(userId,
                        startOfToday)
                .stream()
                .mapToInt(Integer::intValue)
                .sum();
        if (durationSum >= VIDEO_COURSE_UPLOAD_MINUTES_LIMIT * 60) {
            throw new CustomException(VideoErrorCode.VIDEO_EXCEED_UPLOAD_LIMIT);
        }
    }

    @Override
    public void validateReadable(Long userId, Long videoId) {
        if (videoCourseRepository.existsByIdAndUserId(videoId, userId)) {
            return;
        }
        // 추가 검증
        throw new CustomException(VideoErrorCode.VIDEO_CANT_READ);
    }

    @Override
    public void verifyEncodingCompletedAndDeleted(Long parentId) {
        VideoCourse videoCourse = videoCourseRepository.findByCourseId(parentId)
                .orElseThrow(() -> new CustomException(VideoErrorCode.VIDEO_NOT_FOUND));
        if (videoCourse.getEncodingStatus() == EncodingStatus.FAILED) {
            throw new CustomException(VideoErrorCode.VIDEO_ENCODING_FAILED);
        }
        if (videoCourse.getEncodingStatus() == EncodingStatus.FAILED_INVALID_VIDEO_LENGTH) {
            throw new CustomException(VideoErrorCode.VIDEO_ENCODING_FAILED_INVALID_LENGTH);
        }
        if (videoCourse.getEncodingStatus() == EncodingStatus.FAILED_NOT_FOUND_VOICE) {
            throw new CustomException(VideoErrorCode.VIDEO_ENCODING_FAILED_NOT_FOUND_VOICE);
        }
        if (videoCourse.getEncodingStatus() != EncodingStatus.COMPLETED) {
            throw new CustomException(VideoErrorCode.VIDEO_NOT_FINISHED_ENCODING);
        }
    }
}
