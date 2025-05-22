package insty.domain.video.repository;

import insty.model.video.VideoCourse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoCourseRepository extends JpaRepository<VideoCourse, Long> {
}
