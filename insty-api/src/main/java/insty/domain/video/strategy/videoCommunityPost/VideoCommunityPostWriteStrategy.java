package insty.domain.video.strategy.videoCommunityPost;

import insty.domain.video.dto.VideoUploadReq;
import insty.domain.video.repository.VideoCommunityPostRepository;
import insty.domain.video.strategy.VideoWriteStrategy;
import insty.model.user.User;
import insty.model.video.BaseVideo;
import insty.model.video.VideoCommunityPost;
import insty.uuid.UuidProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class VideoCommunityPostWriteStrategy implements VideoWriteStrategy {

    private final UuidProvider uuidProvider;

    private final VideoCommunityPostRepository videoCommunityPostRepository;

    @Override
    public BaseVideo saveVideo(VideoUploadReq req, User user) {
        VideoCommunityPost videoCommunityPost = VideoCommunityPost.create(req.fileName(), uuidProvider.generate(), user);
        return videoCommunityPostRepository.save(videoCommunityPost);
    }
}
