package insty.domain.video.repository;

import insty.model.video.VideoCourse;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VideoCourseRepository extends JpaRepository<VideoCourse, Long> {

    Optional<VideoCourse> findByVideoUuid(UUID videoUuid);

    @Query("SELECT vc.videoUuid FROM VideoCourse vc WHERE vc.course.id = :courseId AND vc.isDeleted = false")
    Optional<UUID> findVideoUuidByCourseId(@Param("courseId") Long courseId);

    Optional<VideoCourse> findByCourseIdAndIsDeleted(Long courseId, boolean isDeleted);

    @Modifying
    @Query("UPDATE VideoCourse vc SET vc.isDeleted = true WHERE vc.course.id = :courseId")
    void deleteLogicallyByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT vc.duration FROM VideoCourse vc "
            + "WHERE vc.encodingStatus != 'FAILED' AND vc.user.id = :userId AND vc.encodingAt >= :encodingAt")
    List<Integer> findEncodingDurationByUserIdAndEncodingAtGreaterThan(@Param("userId") Long userId,
                                                                       @Param("encodingAt") Instant encodingAt);
}
