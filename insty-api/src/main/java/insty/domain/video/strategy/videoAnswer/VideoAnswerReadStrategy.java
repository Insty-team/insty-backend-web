package insty.domain.video.strategy.videoAnswer;

import insty.domain.video.repository.VideoAnswerRepository;
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
public class VideoAnswerReadStrategy implements VideoReadStrategy {

    private final VideoAnswerRepository videoAnswerRepository;

    @Override
    public UUID getVideoUuid(Long parentId) {
        return videoAnswerRepository.findVideoUuidByCourseAnswerId(parentId)
                .orElseThrow(() -> new CustomException(VideoErrorCode.VIDEO_NOT_FOUND));
    }
}
