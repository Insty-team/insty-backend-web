package insty.domain.video.repository;

import insty.model.video.VideoAnswer;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VideoAnswerRepository extends JpaRepository<VideoAnswer, Long> {

    Optional<VideoAnswer> findByVideoUuid(UUID videoUuid);

    @Query("SELECT va.videoUuid FROM VideoAnswer va WHERE va.communityQuestionId = :communityQuestionId AND va.isDeleted = false")
    Optional<UUID> findVideoUuidByCommunityQuestionId(@Param("communityQuestionId") Long communityQuestionId);

    Optional<VideoAnswer> findByCommunityQuestionIdAndIsDeleted(Long communityQuestionId, boolean isDeleted);

    @Query("SELECT va.duration FROM VideoAnswer va "
            + "WHERE va.encodingStatus != 'FAILED' AND va.user.id = :userId AND va.encodingAt >= :encodingAt")
    List<Integer> findEncodingDurationByUserIdAndEncodingAtGreaterThan(@Param("userId") Long userId,
                                                                       @Param("encodingAt") Instant encodingAt);

    boolean existsVideoAnswerByIdAndUserId(Long id, Long userId);
}
