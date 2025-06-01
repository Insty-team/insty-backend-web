package insty.domain.community.reposiotry;

import insty.model.community.CommunityQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CommunityRepository extends JpaRepository<CommunityQuestion, Long> {

    Optional<CommunityQuestion> findById(Long id);
}
