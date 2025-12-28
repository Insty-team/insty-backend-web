package insty.domain.video.strategy;

import java.util.UUID;

public interface VideoReadStrategy {
    /**
     * videoType, 부모 id에 따라 UUID를 조회한다.<br> 조회되지 않거나 처리되지 않은 타입은 404를 반환한다.
     *
     * @param parentId courseId/courseQuestionId/courseAnswerId
     */
    UUID getVideoUuid(Long parentId);
}
