package insty.domain.courseqna.repository;

import insty.model.courseqna.CourseQuestionFile;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface CourseQuestionFileRepository extends JpaRepository<CourseQuestionFile, Long> {

    List<CourseQuestionFile> findAllByCourseQuestionId(Long questionId);

    @Modifying
    @Transactional
    @Query("""
        DELETE FROM CourseQuestionFile cf
        WHERE cf.courseQuestion.id = :questionId
          AND cf.file.id IN :fileIds
    """)
    void deleteByQuestionIdAndFileIdIn(Long questionId, List<Long> fileIds);

    int countByCourseQuestionId(Long questionId);

    @Modifying
    @Transactional
    @Query("""
        DELETE FROM CourseQuestionFile cf
        WHERE cf.courseQuestion.id = :questionId
    """)
    void deleteAllByQuestionId(Long questionId);
}
