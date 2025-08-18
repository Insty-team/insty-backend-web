package insty.domain.community.repository;

import insty.model.community.CommunityQuestionView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface CommunityQuestionViewRepository extends JpaRepository<CommunityQuestionView, Long> {

	@Query("SELECT v FROM CommunityQuestionView v WHERE v.communityQuestion.id = :questionId AND v.userId = :userId")
	Optional<CommunityQuestionView> findByQuestionIdAndUserId(@Param("questionId") Long questionId, @Param("userId") Long userId);

	@Query("SELECT COUNT(a) > 0 FROM CommunityAnswer a WHERE a.communityQuestion.id = :questionId AND a.createdAt > :lastViewedAt AND a.isDeleted = false AND a.user.id <> :viewerId")
	boolean hasNewAnswersAfter(@Param("questionId") Long questionId, @Param("viewerId") Long viewerId, @Param("lastViewedAt") Instant lastViewedAt);

	@Query("SELECT COUNT(a) > 0 FROM CommunityAnswer a WHERE a.communityQuestion.id = :questionId AND a.isDeleted = false AND a.user.id <> :viewerId")
	boolean existsOtherUserAnswers(@Param("questionId") Long questionId, @Param("viewerId") Long viewerId);
}
