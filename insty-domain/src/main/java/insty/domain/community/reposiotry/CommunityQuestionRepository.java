package insty.domain.community.reposiotry;

import insty.model.community.CommunityQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommunityQuestionRepository extends JpaRepository<CommunityQuestion, Long> {

    List <CommunityQuestion> findAllByCourseId(Long courseId);
}
