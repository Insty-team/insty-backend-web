package insty.domain.video.repository;

import insty.model.video.VideoCourse;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VideoCourseRepository extends JpaRepository<VideoCourse, Long> {

    Optional<VideoCourse> findByVideoUuid(UUID videoUuid);

    @Query("SELECT vc.videoUuid FROM VideoCourse vc WHERE vc.courseId = :courseId AND vc.isDeleted = false")
    Optional<UUID> findVideoUuidByCourseId(@Param("courseId") Long courseId);
}
