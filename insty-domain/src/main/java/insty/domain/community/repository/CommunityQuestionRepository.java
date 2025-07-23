package insty.domain.community.repository;

import insty.model.community.CommunityQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommunityQuestionRepository extends JpaRepository<CommunityQuestion, Long> {

    @Query("SELECT cq FROM CommunityQuestion cq WHERE cq.course.id = :courseId AND cq.isDeleted = false")
    List<CommunityQuestion> findAllByCourseId(@Param("courseId") Long courseId);
}
