package insty.domain.community.repository;

import insty.model.community.CommunityAnswerFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommunityAnswerFileRepository extends JpaRepository<CommunityAnswerFile, Long> {

    List<CommunityAnswerFile> findAllByCommunityAnswerId(Long communityAnswerId);

    // Additional query methods can be defined here if needed
}
