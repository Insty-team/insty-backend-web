package insty.domain.course.repository;

import insty.model.course.CourseInstallEnvChecklist;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseInstallEnvChecklistRepository extends JpaRepository<CourseInstallEnvChecklist, Long> {

    List<CourseInstallEnvChecklist> findAllByCourseId(Long courseId);

    void deleteAllByCourseId(Long courseId);
}
