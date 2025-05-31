package insty.domain.course.repository;

import insty.model.course.CourseKeypoint;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseKeypointRepository extends JpaRepository<CourseKeypoint, Long> {
}
