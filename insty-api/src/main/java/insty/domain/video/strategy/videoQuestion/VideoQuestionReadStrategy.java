package insty.domain.video.strategy.videoQuestion;

import insty.domain.video.repository.VideoQuestionRepository;
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
public class VideoQuestionReadStrategy implements VideoReadStrategy {

    private final VideoQuestionRepository videoQuestionRepository;

    @Override
    public UUID getVideoUuid(Long parentId) {
        return videoQuestionRepository.findVideoUuidByCourseQuestionId(parentId)
                .orElseThrow(() -> new CustomException(VideoErrorCode.VIDEO_NOT_FOUND));
    }
}
