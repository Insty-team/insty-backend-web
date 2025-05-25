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

@Service
@RequiredArgsConstructor
public class VideoReader {

    private final VideoCourseRepository videoCourseRepository;
    private final VideoAnswerRepository videoAnswerRepository;
    private final VideoEncodingRepository videoEncodingRepository;

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
