package insty.domain.video.repository;

import insty.model.video.VideoCourse;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoCourseRepository extends JpaRepository<VideoCourse, Long> {

    Optional<VideoCourse> findByVideoUuid(UUID videoUuid);
}
