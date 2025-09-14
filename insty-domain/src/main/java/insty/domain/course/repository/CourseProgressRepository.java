package insty.domain.course.repository;

import insty.model.course.CourseProgress;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseProgressRepository extends JpaRepository<CourseProgress, Long> {

    boolean existsByUserIdAndCourseId(Long userId, Long courseId);
}
