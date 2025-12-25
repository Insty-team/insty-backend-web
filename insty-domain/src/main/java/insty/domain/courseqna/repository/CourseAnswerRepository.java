package insty.domain.courseqna.repository;

import insty.model.courseqna.CourseAnswer;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseAnswerRepository extends JpaRepository<CourseAnswer, Long> {

    @Query("SELECT ca FROM CourseAnswer ca WHERE ca.courseQuestion.id = :questionId AND ca.isDeleted = false")
    List<CourseAnswer> findAllByCourseQuestionId(@Param("questionId") Long questionId);

    @Query("""
        SELECT a FROM CourseAnswer a
        JOIN FETCH a.user u
        WHERE a.courseQuestion.id = :questionId
          AND a.isDeleted = false
        ORDER BY a.createdAt DESC
    """)
    List<CourseAnswer> findAllDetailsWithUserByCourseQuestionId(@Param("questionId") Long questionId);

    @Query(value = """
        SELECT a FROM CourseAnswer a
        JOIN FETCH a.user u
        WHERE a.courseQuestion.id = :questionId
          AND a.isDeleted = false
        ORDER BY a.createdAt DESC
    """,
    countQuery = """
        SELECT COUNT(a.id) FROM CourseAnswer a
        WHERE a.courseQuestion.id = :questionId
          AND a.isDeleted = false
    """)
    Page<CourseAnswer> findAllDetailsWithUserAttachmentsByCourseQuestionIdWithPagination(
            @Param("questionId") Long questionId, Pageable pageable);

    int countByCourseQuestionIdAndIsDeletedFalse(Long courseQuestionId);
    
    @Query("SELECT COUNT(ca) FROM CourseAnswer ca WHERE ca.courseQuestion.id = :questionId AND ca.isAccepted = true AND ca.isDeleted = false")
    int countAcceptedAnswersByQuestionId(@Param("questionId") Long questionId);

    @Query("""
        SELECT DISTINCT a FROM CourseAnswer a
        JOIN FETCH a.user u
        LEFT JOIN FETCH a.attachments att
        LEFT JOIN FETCH att.file f
        WHERE a.courseQuestion.id = :questionId
          AND a.isAccepted = true
          AND a.isDeleted = false
        ORDER BY a.createdAt DESC
    """)
    List<CourseAnswer> findAcceptedAnswersByQuestionId(@Param("questionId") Long questionId);
}
