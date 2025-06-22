package insty.domain.course.repository;

import insty.model.course.CourseRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRequestRepository extends JpaRepository<CourseRequest, Long> {
}
