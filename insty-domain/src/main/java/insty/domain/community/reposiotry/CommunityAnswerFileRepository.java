package insty.domain.community.reposiotry;

import insty.model.community.CommunityAnswerFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityAnswerFileRepository extends JpaRepository<CommunityAnswerFile, Long> {

    // Additional query methods can be defined here if needed
}
