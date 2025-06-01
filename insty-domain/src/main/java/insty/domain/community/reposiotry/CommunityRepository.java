package insty.domain.community.reposiotry;

import insty.model.community.CommunityQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityRepository extends JpaRepository<CommunityQuestion, Long> {
}
