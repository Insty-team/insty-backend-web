package insty.domain.video.strategy.videoCommunityPost;

import insty.domain.video.repository.VideoCommunityPostRepository;
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
public class VideoCommunityPostReadStrategy implements VideoReadStrategy {

    private final VideoCommunityPostRepository videoCommunityPostRepository;

    @Override
    public UUID getVideoUuid(Long parentId) {
        return videoCommunityPostRepository.findVideoUuidByCommunityPostId(parentId)
                .orElseThrow(() -> new CustomException(VideoErrorCode.VIDEO_NOT_FOUND));
    }
}
