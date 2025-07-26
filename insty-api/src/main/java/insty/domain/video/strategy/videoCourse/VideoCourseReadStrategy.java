package insty.domain.video.strategy.videoCourse;

import insty.domain.video.repository.VideoCourseRepository;
import insty.domain.video.strategy.VideoReadStrategy;
import insty.error.VideoErrorCode;
import insty.exception.CustomException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class VideoCourseReadStrategy implements VideoReadStrategy {

    private final VideoCourseRepository videoCourseRepository;

    @Override
    public UUID getVideoUuid(Long parentId) {
        return videoCourseRepository.findVideoUuidByCourseId(parentId)
                .orElseThrow(() -> new CustomException(VideoErrorCode.VIDEO_NOT_FOUND));
    }
}
