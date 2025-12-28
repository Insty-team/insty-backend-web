package insty.domain.courseqna.repository;

import insty.model.courseqna.CourseQuestionView;
import insty.model.courseqna.id.CourseQuestionViewId;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseQuestionViewRepository extends JpaRepository<CourseQuestionView, CourseQuestionViewId> {

	@Query("SELECT v FROM CourseQuestionView v WHERE v.courseQuestion.id = :questionId AND v.courseQuestionViewId.userId = :userId")
	Optional<CourseQuestionView> findByQuestionIdAndUserId(@Param("questionId") Long questionId, @Param("userId") Long userId);

	@Query("SELECT COUNT(a) > 0 FROM CourseAnswer a WHERE a.courseQuestion.id = :questionId AND a.createdAt > :lastViewedAt AND a.isDeleted = false AND a.user.id <> :viewerId")
	boolean hasNewAnswersAfter(@Param("questionId") Long questionId, @Param("viewerId") Long viewerId, @Param("lastViewedAt") Instant lastViewedAt);

	@Query("SELECT COUNT(a) > 0 FROM CourseAnswer a WHERE a.courseQuestion.id = :questionId AND a.isDeleted = false AND a.user.id <> :viewerId")
	boolean existsOtherUserAnswers(@Param("questionId") Long questionId, @Param("viewerId") Long viewerId);
}
