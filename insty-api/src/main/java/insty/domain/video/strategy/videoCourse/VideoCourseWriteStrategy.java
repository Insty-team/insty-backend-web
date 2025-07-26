package insty.domain.video.strategy.videoCourse;

import insty.domain.video.dto.VideoUploadReq;
import insty.domain.video.repository.VideoCourseRepository;
import insty.domain.video.strategy.VideoWriteStrategy;
import insty.model.user.User;
import insty.model.video.BaseVideo;
import insty.model.video.VideoCourse;
import insty.uuid.UuidProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class VideoCourseWriteStrategy implements VideoWriteStrategy {

    private final UuidProvider uuidProvider;

    private final VideoCourseRepository videoCourseRepository;

    @Override
    public BaseVideo saveVideo(VideoUploadReq req, User user) {
        VideoCourse videoCourse = VideoCourse.create(req.fileName(), uuidProvider.generate(), user);
        return videoCourseRepository.save(videoCourse);
    }
}
