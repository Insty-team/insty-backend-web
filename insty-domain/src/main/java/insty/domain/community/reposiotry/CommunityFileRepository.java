package insty.domain.community.reposiotry;

import insty.model.community.CommunityFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityFileRepository extends JpaRepository<CommunityFile, Long> {
}
