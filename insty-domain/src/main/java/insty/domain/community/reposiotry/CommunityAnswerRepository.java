package insty.domain.community.reposiotry;

import insty.model.community.CommunityAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommunityAnswerRepository extends JpaRepository<CommunityAnswer, Long> {

    //Optional<CommunityAnswer> getCommunityAnswer(Long answerId);
}
