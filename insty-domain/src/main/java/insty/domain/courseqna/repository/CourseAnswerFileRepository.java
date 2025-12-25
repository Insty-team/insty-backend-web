package insty.domain.courseqna.repository;

import insty.model.courseqna.CourseAnswerFile;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface CourseAnswerFileRepository extends JpaRepository<CourseAnswerFile, Long> {

    List<CourseAnswerFile> findAllByCourseAnswerId(Long courseAnswerId);

    @Modifying
    @Transactional
    @Query("""
        DELETE FROM CourseAnswerFile caf
        WHERE caf.courseAnswer.id = :answerId
          AND caf.file.id IN :fileIds
    """)
    void deleteByAnswerIdAndFileIdIn(Long answerId, List<Long> fileIds);

    int countByCourseAnswerId(Long courseAnswerId);

    @Modifying
    @Transactional
    @Query("""
        DELETE FROM CourseAnswerFile caf
        WHERE caf.courseAnswer.id = :answerId
    """)
    void deleteAllByAnswerId(Long answerId);
    
    @Query("""
        SELECT att FROM CourseAnswerFile att
        JOIN FETCH att.file f
        WHERE att.courseAnswer.id IN :answerIds
    """)
    List<CourseAnswerFile> findAttachmentsByAnswerIds(@Param("answerIds") List<Long> answerIds);
}
