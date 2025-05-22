package insty.domain.video.implement;

import insty.domain.video.dto.VideoUploadReq;
import insty.domain.video.repository.VideoCourseRepository;
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
    private final UuidProvider uuidProvider;

    public VideoCourse save(VideoUploadReq req) {
        VideoCourse videoCourse = VideoCourse.create(req.fileName(), uuidProvider.generate());
        return videoCourseRepository.save(videoCourse);
    }
}
