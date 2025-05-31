package insty.domain.video.implement;

import insty.domain.video.repository.VideoAnswerRepository;
import insty.domain.video.repository.VideoCourseRepository;
import insty.domain.video.repository.VideoEncodingRepository;
import insty.error.VideoErrorCode;
import insty.exception.CustomException;
import insty.model.video.VideoEncoding;
import insty.model.video.VideoType;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class VideoReader {

    private final VideoCourseRepository videoCourseRepository;
    private final VideoAnswerRepository videoAnswerRepository;
    private final VideoEncodingRepository videoEncodingRepository;

    /**
     * videoType, 부모 id에 따라 UUID를 조회한다.<br> 조회되지 않거나 처리되지 않은 타입은 404를 반환한다.
     *
     * @param videoType COURSE/ANSWER
     * @param parentId
     * @return uuid
     */
    public UUID getVideoUuid(VideoType videoType, Long parentId) {
        if (videoType.equals(VideoType.COURSE)) {
            return videoCourseRepository.findVideoUuidByCourseId(parentId)
                    .orElseThrow(() -> new CustomException(VideoErrorCode.VIDEO_NOT_FOUND));
        }
        if (videoType.equals(VideoType.ANSWER)) {
            return videoAnswerRepository.findVideoUuidByCommunityQuestionId(parentId)
                    .orElseThrow(() -> new CustomException(VideoErrorCode.VIDEO_NOT_FOUND));
        }
        throw new CustomException(VideoErrorCode.VIDEO_NOT_FOUND);
    }

    public VideoEncoding getVideoEncoding(UUID videoUuid) {
        return videoEncodingRepository.findByVideoUuid(videoUuid)
                .orElseThrow(() -> new CustomException(VideoErrorCode.VIDEO_NOT_FOUND));
    }
}
