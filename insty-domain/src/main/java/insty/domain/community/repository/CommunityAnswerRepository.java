package insty.domain.community.repository;

import insty.model.community.CommunityAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommunityAnswerRepository extends JpaRepository<CommunityAnswer, Long> {

    @Query("SELECT ca FROM CommunityAnswer ca WHERE ca.communityQuestion.id = :questionId AND ca.isDeleted = false")
    List<CommunityAnswer> findAllByCommunityQuestionId(@Param("questionId") Long questionId);

    // todo: 비디오 추가되면 fetch join 추가
    @Query("""
        select distinct a from CommunityAnswer a
        join fetch a.user u
        left join fetch a.attachments att
        where a.communityQuestion.id = :questionId
          and a.isDeleted = false
    """)
    List<CommunityAnswer> findAllWithDetailsByCommunityQuestionId(@Param("questionId") Long questionId);
}
