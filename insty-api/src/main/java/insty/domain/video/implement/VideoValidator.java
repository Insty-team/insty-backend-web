package insty.domain.video.implement;

import static insty.constants.VideoConstants.VIDEO_ANSWER_UPLOAD_MINUTES_LIMIT;
import static insty.constants.VideoConstants.VIDEO_COURSE_UPLOAD_MINUTES_LIMIT;

import insty.domain.video.repository.VideoAnswerRepository;
import insty.domain.video.repository.VideoCourseRepository;
import insty.error.VideoErrorCode;
import insty.exception.CustomException;
import insty.model.video.EncodingStatus;
import insty.model.video.VideoAnswer;
import insty.model.video.VideoCourse;
import insty.model.video.VideoType;
import insty.util.FileUtils;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class VideoValidator {

    private static final Map<String, String> EXTENSION_TO_CONTENT_TYPE = Map.of(
            "mp4", "video/mp4",
            "mov", "video/quicktime",
            "webm", "video/webm"
    );

    private final VideoCourseRepository videoCourseRepository;
    private final VideoAnswerRepository videoAnswerRepository;

    /**
     * 처리할 수 있는 영상 타입인지 확인하고, 영상 타입이 파일명과 일치하는지 확인한다.<br> mp4 = video/mp4<br> mov = video/quicktime<br> webm =
     * video/webm<br>
     *
     * @param fileName    fileName.mp4
     * @param contentType video/mp4
     */
    public void validateContentType(String fileName, String contentType) {
        if (!EXTENSION_TO_CONTENT_TYPE.containsValue(contentType)) {
            throw new CustomException(VideoErrorCode.VIDEO_CONTENT_TYPE_ERROR);
        }
        validateEqualsFormat(fileName, contentType);
    }

    private void validateEqualsFormat(String fileName, String contentType) {
        String extension = FileUtils.extractExtension(fileName)
                .orElseThrow(() -> new CustomException(VideoErrorCode.VIDEO_INVALID_FILE_NAME));
        String expectedContentType = EXTENSION_TO_CONTENT_TYPE.get(extension);
        if (!expectedContentType.equals(contentType)) {
            throw new CustomException(VideoErrorCode.VIDEO_TYPE_NOT_MATCH);
        }
    }

    public void validateUploadable() {
        // TODO - 해당 유저가 업로드할 수 있는지 검증(하루 업로드 제한 등)
        // 강의 영상 - 20분, 답변 영상 - 5분
    }

    public void validateVideoCourseUploadable(Long userId) {
        ZoneId koreaZone = ZoneId.of("Asia/Seoul");
        Instant todayInKorea = LocalDate.now(koreaZone).atStartOfDay(koreaZone).toInstant();

        int durationSum = videoCourseRepository.findEncodingDurationByUserIdAndEncodingAtGreaterThan(userId,
                        todayInKorea)
                .stream()
                .mapToInt(Integer::intValue)
                .sum();
        if (durationSum >= VIDEO_COURSE_UPLOAD_MINUTES_LIMIT * 60) {
            throw new CustomException(VideoErrorCode.VIDEO_EXCEED_UPLOAD_LIMIT);
        }
    }

    public void validateVideoAnswerUploadable(Long userId) {
        ZoneId koreaZone = ZoneId.of("Asia/Seoul");
        Instant todayInKorea = LocalDate.now(koreaZone).atStartOfDay(koreaZone).toInstant();

        int durationSum = videoAnswerRepository.findEncodingDurationByUserIdAndEncodingAtGreaterThan(userId,
                        todayInKorea)
                .stream()
                .mapToInt(Integer::intValue)
                .sum();
        if (durationSum >= VIDEO_ANSWER_UPLOAD_MINUTES_LIMIT * 60) {
            throw new CustomException(VideoErrorCode.VIDEO_EXCEED_UPLOAD_LIMIT);
        }
    }

    public void validateReadable(VideoType videoType, Long id) {
        // TODO - 해당 유저가 영상을 조회할 수 있는지 검증(영상을 업로드한 사람인지 또는 구매한 사람인지)
    }

    /**
     * 인코딩이 완료된 영상만 조회할 수 있다.<br> 가상 삭제에 주의한다.
     *
     * @param videoType
     * @param courseId
     */
    public void verifyEncodingCompleted(VideoType videoType, Long courseId) {
        if (videoType.equals(VideoType.COURSE)) {
            VideoCourse videoCourse = videoCourseRepository.findByCourseIdAndIsDeleted(courseId, false)
                    .orElseThrow(() -> new CustomException(VideoErrorCode.VIDEO_NOT_FOUND));
            if (videoCourse.getEncodingStatus() != EncodingStatus.COMPLETED) {
                throw new CustomException(VideoErrorCode.VIDEO_NOT_FINISHED_ENCODING);
            }
            return;
        }
        if (videoType.equals(VideoType.ANSWER)) {
            VideoAnswer videoAnswer = videoAnswerRepository.findByCommunityQuestionIdAndIsDeleted(courseId, false)
                    .orElseThrow(() -> new CustomException(VideoErrorCode.VIDEO_NOT_FOUND));
            if (videoAnswer.getEncodingStatus() != EncodingStatus.COMPLETED) {
                throw new CustomException(VideoErrorCode.VIDEO_NOT_FINISHED_ENCODING);
            }
            return;
        }
        throw new CustomException(VideoErrorCode.VIDEO_NOT_FOUND);
    }
}
