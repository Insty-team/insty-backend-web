package insty.domain.video.repository;

import insty.model.video.VideoAnswer;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VideoAnswerRepository extends JpaRepository<VideoAnswer, Long> {

    Optional<VideoAnswer> findByVideoUuid(UUID videoUuid);

    @Query("SELECT va.videoUuid FROM VideoAnswer va WHERE va.communityQuestionId = :communityQuestionId AND va.isDeleted = false")
    Optional<UUID> findVideoUuidByCommunityQuestionId(@Param("communityQuestionId") Long communityQuestionId);
}
