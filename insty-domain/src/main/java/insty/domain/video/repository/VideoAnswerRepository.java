package insty.domain.video.repository;

import insty.model.video.VideoAnswer;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VideoAnswerRepository extends JpaRepository<VideoAnswer, Long> {

    Optional<VideoAnswer> findByVideoUuid(UUID videoUuid);

    @Query("SELECT va.videoUuid FROM VideoAnswer va WHERE va.communityAnswer.id = :communityAnswerId AND va.isDeleted = false")
    Optional<UUID> findVideoUuidByCommunityAnswerId(@Param("communityAnswerId") Long communityAnswerId);

    Optional<VideoAnswer> findByCommunityAnswerIdAndIsDeleted(Long communityAnswerId, boolean isDeleted);

    @Modifying
    @Query("UPDATE VideoAnswer va SET va.isDeleted = true WHERE va.id = :id")
    void deleteLogicallyById(@Param("id") Long id);

    @Query("SELECT va.duration FROM VideoAnswer va "
            + "WHERE va.encodingStatus != 'FAILED' AND va.user.id = :userId AND va.encodingAt >= :encodingAt")
    List<Integer> findEncodingDurationByUserIdAndEncodingAtGreaterThan(@Param("userId") Long userId,
                                                                       @Param("encodingAt") Instant encodingAt);

    boolean existsByIdAndUserId(Long id, Long userId);
}
