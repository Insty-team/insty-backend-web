package insty.domain.community.repository;

import insty.model.community.CommunityQuestion;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommunityQuestionRepository extends JpaRepository<CommunityQuestion, Long> {

    @Query("""
        SELECT q FROM CommunityQuestion q
        JOIN FETCH q.user u
        LEFT JOIN FETCH q.attachments att
        LEFT JOIN FETCH att.file f
        WHERE q.id = :questionId
    """)
    Optional<CommunityQuestion> findDetailsWithUserAttachmentsById(@Param("questionId") Long questionId);

    @Query("SELECT cq FROM CommunityQuestion cq WHERE cq.course.id = :courseId AND cq.isDeleted = false")
    List<CommunityQuestion> findAllByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT c.user.id FROM CommunityQuestion q JOIN q.course c WHERE q.id = :questionId")
    Long findCreatorIdByQuestionId(@Param("questionId") Long questionId);

    @Query("SELECT cq.course.id, COUNT(cq) FROM CommunityQuestion cq WHERE cq.course.id IN :courseIds AND cq.isDeleted = false GROUP BY cq.course.id")
    List<Object[]> countByCourseIds(@Param("courseIds")List<Long> courseIds);
}
