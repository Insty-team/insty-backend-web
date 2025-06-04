package insty.domain.community.reposiotry;

import insty.model.community.CommunityQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommunityQuestionRepository extends JpaRepository<CommunityQuestion, Long> {

    //Optional<CommunityQuestion> getCommunityQuestion(Long id);
}
