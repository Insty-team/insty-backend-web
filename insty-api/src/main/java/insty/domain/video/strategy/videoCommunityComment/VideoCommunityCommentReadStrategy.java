package insty.domain.video.strategy.videoCommunityComment;

import insty.domain.video.repository.VideoCommunityCommentRepository;
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
public class VideoCommunityCommentReadStrategy implements VideoReadStrategy {

    private final VideoCommunityCommentRepository videoCommunityCommentRepository;

    @Override
    public UUID getVideoUuid(Long parentId) {
        return videoCommunityCommentRepository.findVideoUuidByCommunityCommentId(parentId)
                .orElseThrow(() -> new CustomException(VideoErrorCode.VIDEO_NOT_FOUND));
    }
}
