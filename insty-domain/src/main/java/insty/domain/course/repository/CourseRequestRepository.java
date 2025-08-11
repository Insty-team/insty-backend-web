package insty.domain.course.repository;

import insty.model.course.CourseRequest;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRequestRepository extends JpaRepository<CourseRequest, Long> {
    List<CourseRequest> findByRecipientId(Long userId);

    void deleteAllByRequesterId(Long requesterId);
}
