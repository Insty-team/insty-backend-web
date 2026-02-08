package insty.domain.courseqna.repository;

import insty.model.courseqna.CourseQuestion;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseQuestionRepository extends JpaRepository<CourseQuestion, Long> {

    @Query("""
        SELECT q FROM CourseQuestion q
        JOIN FETCH q.user u
        JOIN FETCH q.course c
        LEFT JOIN FETCH q.attachments att
        LEFT JOIN FETCH att.file f
        WHERE q.id = :questionId
    """)
    Optional<CourseQuestion> findDetailsWithUserAttachmentsById(@Param("questionId") Long questionId);

    @Query("SELECT cq FROM CourseQuestion cq WHERE cq.course.id = :courseId AND cq.isDeleted = false")
    List<CourseQuestion> findAllByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT c.user.id FROM CourseQuestion q JOIN q.course c WHERE q.id = :questionId")
    Long findCreatorIdByQuestionId(@Param("questionId") Long questionId);
}
