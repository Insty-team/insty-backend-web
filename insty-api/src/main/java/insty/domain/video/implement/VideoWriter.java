package insty.domain.video.implement;

import insty.domain.video.dto.VideoUploadReq;
import insty.domain.video.repository.VideoRepository;
import insty.model.video.Video;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class VideoWriter {

    private final VideoRepository videoRepository;

    public Video save(VideoUploadReq req) {
        Video video = Video.create(req.fileName());
        return videoRepository.save(video);
    }
}
