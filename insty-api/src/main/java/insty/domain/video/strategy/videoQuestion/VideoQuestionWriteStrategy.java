package insty.domain.video.strategy.videoQuestion;

import insty.domain.video.dto.VideoUploadReq;
import insty.domain.video.repository.VideoQuestionRepository;
import insty.domain.video.strategy.VideoWriteStrategy;
import insty.model.user.User;
import insty.model.video.BaseVideo;
import insty.model.video.VideoQuestion;
import insty.uuid.UuidProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class VideoQuestionWriteStrategy implements VideoWriteStrategy {

    private final UuidProvider uuidProvider;

    private final VideoQuestionRepository videoQuestionRepository;

    @Override
    public BaseVideo saveVideo(VideoUploadReq req, User user) {
        VideoQuestion videoQuestion = VideoQuestion.create(req.fileName(), uuidProvider.generate(), user);
        return videoQuestionRepository.save(videoQuestion);
    }
}
