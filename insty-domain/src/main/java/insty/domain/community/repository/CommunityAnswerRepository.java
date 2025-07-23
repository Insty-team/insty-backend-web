package insty.domain.community.repository;

import insty.model.community.CommunityAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommunityAnswerRepository extends JpaRepository<CommunityAnswer, Long> {

    @Query("SELECT ca FROM CommunityAnswer ca WHERE ca.communityQuestion.id = :questionId AND ca.isDeleted = false")
    List<CommunityAnswer> findAllByCommunityQuestionId(@Param("questionId") Long questionId);
}
