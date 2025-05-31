package insty.domain.course.repository;

import insty.model.course.CourseKeypoint;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseKeypointRepository extends JpaRepository<CourseKeypoint, Long> {

    List<CourseKeypoint> findAllByCourseId(Long courseId);
}
