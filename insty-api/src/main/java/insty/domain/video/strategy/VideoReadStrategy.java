package insty.domain.video.strategy;

import java.util.UUID;

public interface VideoReadStrategy {
    UUID getVideoUuid(Long parentId);
}
