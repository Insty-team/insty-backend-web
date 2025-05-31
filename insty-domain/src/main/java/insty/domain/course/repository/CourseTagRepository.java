package insty.domain.course.repository;

import insty.model.course.CourseTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseTagRepository extends JpaRepository<CourseTag, Long> {
}
