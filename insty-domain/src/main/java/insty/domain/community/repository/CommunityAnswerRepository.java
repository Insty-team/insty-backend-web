package insty.domain.community.repository;

import insty.model.community.CommunityAnswer;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommunityAnswerRepository extends JpaRepository<CommunityAnswer, Long> {

    @Query("SELECT ca FROM CommunityAnswer ca WHERE ca.communityQuestion.id = :questionId AND ca.isDeleted = false")
    List<CommunityAnswer> findAllByCommunityQuestionId(@Param("questionId") Long questionId);

    @Query("""
        SELECT DISTINCT a FROM CommunityAnswer a
        JOIN FETCH a.user u
        LEFT JOIN FETCH a.attachments att
        LEFT JOIN FETCH att.file f
        WHERE a.communityQuestion.id = :questionId
          AND a.isDeleted = false
        ORDER BY a.createdAt DESC
    """)
    List<CommunityAnswer> findAllDetailsWithUserAttachmentsByCommunityQuestionId(@Param("questionId") Long questionId);

    int countByCommunityQuestionIdAndIsDeletedFalse(Long communityQuestionId);
    
    @Query("SELECT COUNT(ca) FROM CommunityAnswer ca WHERE ca.communityQuestion.id = :questionId AND ca.isAccepted = true AND ca.isDeleted = false")
    int countAcceptedAnswersByQuestionId(@Param("questionId") Long questionId);
}
