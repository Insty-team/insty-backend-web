package insty.domain.video.repository;

import insty.model.video.EncodingStatus;
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

    @Query("SELECT va.videoUuid FROM VideoAnswer va WHERE va.courseAnswer.id = :courseAnswerId AND va.isDeleted = false")
    Optional<UUID> findVideoUuidByCourseAnswerId(@Param("courseAnswerId") Long courseAnswerId);

    Optional<VideoAnswer> findByCourseAnswerIdAndIsDeleted(Long courseAnswerId, boolean isDeleted);

    @Query("SELECT va FROM VideoAnswer va WHERE va.courseAnswer.id IN :answerIds AND va.isDeleted = false")
    List<VideoAnswer> findAllByCourseAnswerIds(@Param("answerIds") List<Long> answerIds);

    @Modifying
    @Query("UPDATE VideoAnswer va SET va.isDeleted = true WHERE va.id = :id")
    void deleteLogicallyById(@Param("id") Long id);

    @Query("SELECT va.duration FROM VideoAnswer va "
            + "WHERE va.user.id = :userId AND va.encodingAt >= :encodingAt AND va.encodingStatus in :encodingStatus")
    List<Integer> findEncodingDuration(@Param("userId") Long userId, @Param("encodingAt") Instant encodingAt,
                                       @Param("encodingStatus") List<EncodingStatus> encodingStatuses);

    boolean existsByIdAndUserId(Long id, Long userId);
}
