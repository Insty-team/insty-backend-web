package insty.domain.video.strategy.videoAnswer;

import insty.domain.video.dto.VideoUploadReq;
import insty.domain.video.repository.VideoAnswerRepository;
import insty.domain.video.strategy.VideoWriteStrategy;
import insty.model.user.User;
import insty.model.video.BaseVideo;
import insty.model.video.VideoAnswer;
import insty.model.video.VideoCourse;
import insty.uuid.UuidProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class VideoAnswerWriteStrategy implements VideoWriteStrategy {

    private final UuidProvider uuidProvider;

    private final VideoAnswerRepository videoAnswerRepository;

    @Override
    public BaseVideo saveVideo(VideoUploadReq req, User user) {
        VideoAnswer videoAnswer = VideoAnswer.create(req.fileName(), uuidProvider.generate(), user);
        return videoAnswerRepository.save(videoAnswer);
    }
}
