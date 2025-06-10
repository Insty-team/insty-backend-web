package insty.domain.community.reposiotry;

import insty.model.community.CommunityAttactments;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityAttachmentRepository extends JpaRepository<CommunityAttactments, Long> {
}
