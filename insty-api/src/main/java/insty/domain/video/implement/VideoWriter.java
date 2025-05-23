package insty.domain.video.implement;

import insty.domain.video.dto.VideoUploadReq;
import insty.domain.video.repository.VideoAnswerRepository;
import insty.domain.video.repository.VideoCourseRepository;
import insty.model.video.VideoAnswer;
import insty.model.video.VideoCourse;
import insty.uuid.UuidProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class VideoWriter {

    private final VideoCourseRepository videoCourseRepository;
    private final VideoAnswerRepository videoAnswerRepository;
    private final UuidProvider uuidProvider;

    public VideoCourse saveVideoCourse(VideoUploadReq req) {
        VideoCourse videoCourse = VideoCourse.create(req.fileName(), uuidProvider.generate());
        return videoCourseRepository.save(videoCourse);
    }

    public VideoAnswer saveVideoAnswer(VideoUploadReq req) {
        VideoAnswer videoCourse = VideoAnswer.create(req.fileName(), uuidProvider.generate());
        return videoAnswerRepository.save(videoCourse);
    }
}
