package insty.domain.community.reposiotry;

import insty.model.community.CommunityAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityAnswerRepository extends JpaRepository<CommunityAnswer, Long> {
}
