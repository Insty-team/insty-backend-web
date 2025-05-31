package insty.domain.course.repository;

import insty.model.course.CourseInstallEnvChecklist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseInstallEnvChecklistRepository extends JpaRepository<CourseInstallEnvChecklist, Long> {
}
