package insty.domain.video.repository;

import insty.model.video.VideoQuestion;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VideoQuestionRepository extends JpaRepository<VideoQuestion, Long> {

    Optional<VideoQuestion> findByVideoUuid(UUID videoUuid);

    @Query("SELECT va.videoUuid FROM VideoQuestion va WHERE va.communityQuestion.id = :communityQuestionId AND va.isDeleted = false")
    Optional<UUID> findVideoUuidByCommunityQuestionId(@Param("communityQuestionId") Long communityQuestionId);

    Optional<VideoQuestion> findByCommunityQuestionIdAndIsDeleted(Long communityQuestionId, boolean isDeleted);

    @Modifying
    @Query("UPDATE VideoQuestion vq SET vq.isDeleted = true WHERE vq.id = :id")
    void deleteLogicallyById(@Param("id") Long id);

    @Query("SELECT va.duration FROM VideoQuestion va "
            + "WHERE va.encodingStatus != 'FAILED' AND va.user.id = :userId AND va.encodingAt >= :encodingAt")
    List<Integer> findEncodingDurationByUserIdAndEncodingAtGreaterThan(@Param("userId") Long userId,
                                                                       @Param("encodingAt") Instant encodingAt);

    boolean existsByIdAndUserId(Long id, Long userId);
}
