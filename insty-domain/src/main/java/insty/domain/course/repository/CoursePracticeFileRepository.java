package insty.domain.course.repository;

import insty.model.course.CoursePracticeFile;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoursePracticeFileRepository extends JpaRepository<CoursePracticeFile, Long> {

    void deleteByCourseIdAndPracticeFileIdIn(Long courseId, List<Long> practiceFileId);
}
