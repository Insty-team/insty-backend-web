package insty.domain.video.strategy.videoCommunityComment;

import insty.domain.video.dto.VideoUploadReq;
import insty.domain.video.repository.VideoCommunityCommentRepository;
import insty.domain.video.strategy.VideoWriteStrategy;
import insty.model.user.User;
import insty.model.video.BaseVideo;
import insty.model.video.VideoCommunityComment;
import insty.uuid.UuidProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class VideoCommunityCommentWriteStrategy implements VideoWriteStrategy {

    private final UuidProvider uuidProvider;

    private final VideoCommunityCommentRepository videoCommunityCommentRepository;

    @Override
    public BaseVideo saveVideo(VideoUploadReq req, User user) {
        VideoCommunityComment videoCommunityComment = VideoCommunityComment.create(req.fileName(), uuidProvider.generate(), user);
        return videoCommunityCommentRepository.save(videoCommunityComment);
    }
}
