package insty.domain.community.repository;

import insty.model.community.CommunityQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommunityQuestionRepository extends JpaRepository<CommunityQuestion, Long> {

    List <CommunityQuestion> findAllByCourseId(Long courseId);
}
