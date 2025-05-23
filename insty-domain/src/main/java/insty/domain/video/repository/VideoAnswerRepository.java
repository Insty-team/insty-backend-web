package insty.domain.video.repository;

import insty.model.video.VideoAnswer;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoAnswerRepository extends JpaRepository<VideoAnswer, Long> {

    Optional<VideoAnswer> findByVideoUuid(UUID videoUuid);

    Optional<UUID> findVideoUuidByCommunityQuestionId(Long communityQuestionId);
}
